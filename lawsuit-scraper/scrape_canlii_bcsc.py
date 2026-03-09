import os
import sys
import time
from urllib.parse import urljoin

import requests
from bs4 import BeautifulSoup
import psycopg2

START_URL = "https://proactio.ca/en/class-action/"  # Proactio class actions directory

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
    "Accept-Language": "en-CA,en;q=0.9",
}

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
        "you don't have to do anything",
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


def upsert_lawsuit(cur, title: str, source_url: str, claim_required, proof_required: bool, difficulty: str, compensation_type: str):
    cur.execute(
        """
        INSERT INTO lawsuits (title, source_url, claim_required, proof_required, difficulty, compensation_type)
        VALUES (%s, %s, %s, %s, %s, %s)
        ON CONFLICT (source_url) DO UPDATE SET
          title = EXCLUDED.title,
          claim_required = EXCLUDED.claim_required,
          proof_required = EXCLUDED.proof_required,
          difficulty = EXCLUDED.difficulty,
          compensation_type = EXCLUDED.compensation_type,
          scraped_at = NOW()
        """,
        (title, source_url, claim_required, proof_required, difficulty, compensation_type),
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
    print(f"Fetching: {START_URL}")
    resp = requests.get(START_URL, headers=HEADERS, timeout=30)
    resp.raise_for_status()

    soup = BeautifulSoup(resp.text, "html.parser")

    # Collect class action case links from the directory.
    # The directory page contains many links to individual case pages.
    links = []
    for a in soup.select("a[href]"):
        href = (a.get("href") or "").strip()
        if not href:
            continue

        full = urljoin(START_URL, href)

        # Keep only /en/class-action/<slug>/ case pages (skip the directory itself)
        if full.startswith("https://proactio.ca/en/class-action/") and full != START_URL:
            title = a.get_text(" ", strip=True)
            if title:
                links.append((title, full))

    # De-duplicate while keeping order
    seen = set()
    unique = []
    for t, u in links:
        # Normalize by stripping querystring and trailing slashes
        u_norm = u.split("?")[0].rstrip("/") + "/"
        if u_norm not in seen:
            seen.add(u_norm)
            unique.append((t, u_norm))

    print(f"Found {len(unique)} case links on this page")
    time.sleep(1.0)

    conn = connect_db()
    try:
        with conn.cursor() as cur:
            max_items = 20
            for i, (dir_title, url) in enumerate(unique[:max_items], start=1):
                try:
                    print(f"[{i}/{max_items}] Fetching case: {url}")
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

                    upsert_lawsuit(cur, title, url, claim_required, proof_required, difficulty, compensation_type)
                    print(
                        f"[{i}/{max_items}] saved: {title} | claim_required={claim_required} proof_required={proof_required} difficulty={difficulty} compensation_type={compensation_type}"
                    )

                except Exception as e:
                    print(f"[{i}/{max_items}] ERROR for {url}: {e}")

                time.sleep(2.0)  # be polite

    finally:
        conn.close()

    print("Done.")

if __name__ == "__main__":
    main()