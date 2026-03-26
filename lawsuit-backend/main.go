package main

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"os"
	"strconv"

	"github.com/jackc/pgx/v5/pgxpool"
)

type Lawsuit struct {
	ID               int    `json:"id"`
	Title            string `json:"title"`
	ClaimRequired    bool   `json:"claim_required"`
	ProofRequired    bool   `json:"proof_required"`
	Difficulty       string `json:"difficulty"`
	CompensationType string `json:"compensation_type"`
	CaseStatus       string `json:"case_status"`
	SourceURL        string `json:"source_url"`
}

func main() {
	// 1. Read DB connection string
	dbURL := os.Getenv("DATABASE_URL")
	if dbURL == "" {
		log.Fatal("DATABASE_URL is not set")
	}

	// 2. Connect to PostgreSQL
	db, err := pgxpool.New(context.Background(), dbURL)
	if err != nil {
		log.Fatal(err)
	}
	defer db.Close()

	// 3. HTTP handler
	/*http.HandleFunc("/lawsuits", func(w http.ResponseWriter, r *http.Request) {
		rows, err := db.Query(context.Background(),
			`SELECT id, title, compensation, proof_required, difficulty FROM lawsuits`)
		if err != nil {
			http.Error(w, err.Error(), 500)
			return
		}
		defer rows.Close()

		lawsuits := []Lawsuit{}

		for rows.Next() {
			var l Lawsuit
			err := rows.Scan(&l.ID, &l.Title, &l.Compensation, &l.ProofRequired, &l.Difficulty)
			if err != nil {
				http.Error(w, err.Error(), 500)
				return
			}
			lawsuits = append(lawsuits, l)
		}

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(lawsuits)
	})*/
	http.HandleFunc("/lawsuits", func(w http.ResponseWriter, r *http.Request) {
		query := `
			SELECT id, title, claim_required, proof_required, difficulty, compensation_type, case_status, source_url
			FROM lawsuits
			WHERE 1=1
		`
		args := []any{}
		argPos := 1
		qp := r.URL.Query()

		// claim_required=true/false (optional)
		if v := qp.Get("claim_required"); v != "" {
			if v != "true" && v != "false" {
				http.Error(w, "claim_required must be true or false", http.StatusBadRequest)
				return
			}
			query += " AND claim_required = $" + strconv.Itoa(argPos)
			args = append(args, v == "true")
			argPos++
		}

		//Proof_required True/False (optional)
		if v := qp.Get("proof_required"); v != "" {
			if v != "true" && v != "false" {
				http.Error(w, "proof_required must be true or false", http.StatusBadRequest)
				return
			}
			query += " AND proof_required = $" + strconv.Itoa(argPos)
			args = append(args, v == "true")
			argPos++
		}

		// compensation_type=money|non_money|unknown (optional)
		if v := qp.Get("compensation_type"); v != "" {
			if v != "money" && v != "non_money" && v != "unknown" {
				http.Error(w, "compensation_type must be money, non_money, or unknown", http.StatusBadRequest)
				return
			}
			query += " AND compensation_type = $" + strconv.Itoa(argPos)
			args = append(args, v)
			argPos++
		}

		//Difficulty easy/medium/hard (optional)
		if v := qp.Get("difficulty"); v != "" {
			if v != "easy" && v != "medium" && v != "hard" {
				http.Error(w, "difficulty must be easy, medium or hard", http.StatusBadRequest)
				return
			}
			query += " AND difficulty = $" + strconv.Itoa(argPos)
			args = append(args, v)
			argPos++
		}

		query += " ORDER BY id"
		//Combine query command for database, sign value to rows
		rows, err := db.Query(context.Background(), query, args...)
		if err != nil {
			http.Error(w, err.Error(), 500)
			return
		}
		defer rows.Close() //close after every fetch

		lawsuits := []Lawsuit{}
		for rows.Next() {
			var l Lawsuit
			if err := rows.Scan(&l.ID, &l.Title, &l.ClaimRequired, &l.ProofRequired, &l.Difficulty, &l.CompensationType, &l.CaseStatus, &l.SourceURL); err != nil {
				http.Error(w, err.Error(), 500)
				return
			}
			lawsuits = append(lawsuits, l)
		}

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(lawsuits)
	})

	log.Println("Server running on http://localhost:8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
