import os
import sys
import psycopg2

TEST_ROW = {
    "title": "Scraper Insert Test",
    "source_url": "https://example.com/scraper-test",
    "compensation": True,
    "proof_required": False,
    "difficulty": "medium",
}

def main():
    db_url = os.getenv("DATABASE_URL")
    if not db_url:
        print("ERROR: DATABASE_URL is not set")
        print('Example:\nexport DATABASE_URL="postgres://lawsuit_app:lawsuit@localhost:5432/lawsuit_db?sslmode=disable"')
        sys.exit(1)

    conn = psycopg2.connect(db_url)
    conn.autocommit = True

    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO lawsuits (title, source_url, compensation, proof_required, difficulty)
            VALUES (%s, %s, %s, %s, %s)
            ON CONFLICT (source_url) DO UPDATE SET
              title = EXCLUDED.title,
              compensation = EXCLUDED.compensation,
              proof_required = EXCLUDED.proof_required,
              difficulty = EXCLUDED.difficulty,
              scraped_at = NOW()
            RETURNING id;
            """,
            (
                TEST_ROW["title"],
                TEST_ROW["source_url"],
                TEST_ROW["compensation"],
                TEST_ROW["proof_required"],
                TEST_ROW["difficulty"],
            ),
        )
        new_id = cur.fetchone()[0]
        print(f"Inserted/updated row id={new_id}")

    conn.close()

if __name__ == "__main__":
    main()