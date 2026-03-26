import os
import sys
import time
from urllib.parse import urljoin, urlparse, parse_qs

import requests
from bs4 import BeautifulSoup
import psycopg2

START_URL = "https://proactio.ca/en/class-action/"  # Proactio class actions directory

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
    "Accept-Language": "en-CA,en;q=0.9",
}

def normalize_url(url: str) -> str:
    return url.split("?")[0].rstrip("/") + "/"

# --- Classification helpers (simple keyword rules, no PDF parsing yet) ---

def compute_difficulty(claim_required, proof_required: bool) -> str:
    if claim_required == "any":
        return "medium"
    if (not claim_required) and (not proof_required):
        return "easy"
    if (not claim_required) and proof_required:
        return "medium"
    return "hard"  


def infer_claim_required(case_soup: BeautifulSoup, text: str):
    t = text.lower()

    # Rule 1: if a Claim button exists, claim is required
    claim_button = case_soup.select_one("a.hero__subscription.btn--primary.big")
    if claim_button:
        return True

    # Rule 2: strong no-claim phrases
    no_claim_phrases = [
        "not need to do anything",
        "have nothing to do for the moment",
        "do not have to complete any claim form",
        "do not have to submit a claim",
        "you don’t have to do anything",
        "you do not have to do anything",
    ]
    if any(p in t for p in no_claim_phrases):
        return False

    # Rule 3: otherwise unknown/any
    return True


def infer_proof_required(case_soup: BeautifulSoup) -> bool:
    criteria_block = case_soup.select_one("div.criterias__content.content")
    return criteria_block is not None


def infer_compensation_type(text: str) -> str:
    """Return 'money', 'non_money', or 'unknown' based on HTML text only (no PDF parsing)."""
    t = text.lower()

    money_markers = [
        "$",
        "cad",
        "million",
        "payment",
        "cash",
        "compensation",
        "indemnity",
        "refund",
        "reimbursement",
        "settlement amount",
    ]
    if any(m in t for m in money_markers):
        return "money"

    non_money_markers = [
        "credit monitoring",
        "voucher",
        "coupon",
        "replacement",
        "injunctive",
        "policy change",
        "program changes",
        "non-monetary",
        "non monetary",
    ]
    if any(m in t for m in non_money_markers):
        return "non_money"

    return "unknown"


def infer_case_status(text: str):
    """Return 'in progress' or 'completed' when the status is visible in card/page text."""
    t = text.lower()

    if "in progress" in t:
        return "In Progress"
    if "completed" in t or "complete" in t:
        return "Completed"

    return None


def upsert_lawsuit(cur, title: str, source_url: str, claim_required, proof_required: bool, difficulty: str, compensation_type: str, case_status):
    cur.execute(
        """
        INSERT INTO lawsuits (title, source_url, claim_required, proof_required, difficulty, compensation_type, case_status)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        ON CONFLICT (source_url) DO UPDATE SET
          title = EXCLUDED.title,
          claim_required = EXCLUDED.claim_required,
          proof_required = EXCLUDED.proof_required,
          difficulty = EXCLUDED.difficulty,
          compensation_type = EXCLUDED.compensation_type,
          case_status = EXCLUDED.case_status,
          scraped_at = NOW()
        """,
        (title, source_url, claim_required, proof_required, difficulty, compensation_type, case_status),
    )

def connect_db():
    db_url = os.getenv("DATABASE_URL")
    if not db_url:
        print("ERROR: DATABASE_URL is not set")
        sys.exit(1)
    conn = psycopg2.connect(db_url)
    conn.autocommit = True
    return conn

def main():
    print(f"Fetching directory: {START_URL}")
    

    # Build directory pages directly instead of trying to discover pagination buttons.
    # Proactio serves page 2, page 3, etc. at ?_paged=N, but those buttons are not
    # reliably present in the HTML returned to requests.
    directory_pages = []
    page_number = 1

    while True:
        if page_number == 1:
            page_url = START_URL
        else:
            page_url = f"{START_URL}?_paged={page_number}"

        print(f"Checking directory page: {page_url}")
        page_resp = requests.get(page_url, headers=HEADERS, timeout=30)
        page_resp.raise_for_status()
        page_soup = BeautifulSoup(page_resp.text, "html.parser")

        page_links = []
        for a in page_soup.select("a[href]"):
            href = (a.get("href") or "").strip()
            if not href:
                continue

            full = urljoin(page_url, href)
            full_norm = normalize_url(full)

            # Keep only actual case pages, not the directory pages themselves.
            if not full_norm.startswith("https://proactio.ca/en/class-action/"):
                continue
            if full_norm == normalize_url(START_URL):
                continue

            parsed = urlparse(full)
            qs = parse_qs(parsed.query)
            if parsed.path.rstrip("/") == "/en/class-action" and "_paged" in qs:
                continue

            title_el = a.select_one(".item__title")
            title = title_el.get_text(" ", strip=True) if title_el else ""

            state_el = a.select_one(".item__state")
            state_text = state_el.get_text(" ", strip=True) if state_el else ""
            case_status = infer_case_status(state_text)

            if title:
                page_links.append((title, full_norm, case_status))

        if not page_links:
            break

        directory_pages.append((page_url, page_soup, page_links))
        page_number += 1
        time.sleep(1.0)

    print(f"Found {len(directory_pages)} directory pages")

    # Collect case links from every directory page already fetched above
    links = []
    for page_index, (page_url, _page_soup, page_links) in enumerate(directory_pages, start=1):
        print(f"[{page_index}/{len(directory_pages)}] Reading directory page: {page_url}")
        links.extend(page_links)

    # De-duplicate while keeping order
    seen = set()
    unique = []
    for t, u, s in links:
        u_norm = normalize_url(u)
        if u_norm not in seen:
            seen.add(u_norm)
            unique.append((t, u_norm, s))

    print(f"Found {len(unique)} case links across all directory pages")
    time.sleep(1.0)

    conn = connect_db()
    try:
        with conn.cursor() as cur:
            max_items = 30
            total = min(len(unique), max_items)
            for i, (dir_title, url, dir_case_status) in enumerate(unique[:max_items], start=1):
                try:
                    print(f"[{i}/{total}] Fetching case: {url}")
                    case_resp = requests.get(url, headers=HEADERS, timeout=30)
                    case_resp.raise_for_status()
                    case_soup = BeautifulSoup(case_resp.text, "html.parser")

                    # Title: prefer <h1>, fallback to directory anchor text
                    h1 = case_soup.select_one("h1")
                    title = (h1.get_text(" ", strip=True) if h1 else dir_title).strip()
                    if not title:
                        title = dir_title

                    # Extract visible text (HTML only)
                    text = case_soup.get_text(" ", strip=True)

                    claim_required = infer_claim_required(case_soup, text)
                    proof_required = infer_proof_required(case_soup)
                    difficulty = compute_difficulty(claim_required, proof_required)
                    compensation_type = infer_compensation_type(text)
                    case_status = dir_case_status

                    upsert_lawsuit(cur, title, url, claim_required, proof_required, difficulty, compensation_type, case_status)
                    print(
                        f"[{i}/{total}] saved: {title} | claim_required={claim_required} proof_required={proof_required} difficulty={difficulty} compensation_type={compensation_type} case_status={case_status}"
                    )

                except Exception as e:
                    print(f"[{i}/{total}] ERROR for {url}: {e}")

                time.sleep(2.0)  # be polite

    finally:
        conn.close()

    print("Done.")

if __name__ == "__main__":
    main()