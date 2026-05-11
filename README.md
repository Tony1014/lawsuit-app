# Lawsuit Information App (Canada)

This project is a simple full-stack application that collects and displays information about class action lawsuits in BC.

It is built for learning purposes to explore full-stack software development, mobile application architecture, backend APIs, database integration, and web scraping.

The application allows users to browse lawsuit information, filter cases, and open official source links for more details.
___

## Features
- View class action lawsuit information
- Filter lawsuits by: 
  - Claim required
  - Proof required
  - Difficulty level
- View compensation type
- View lawsuit status
- Open official source links
- Real-time data fetched from backend API
___

## Architecture
This project follows an MVVM (Model-View-ViewModel) architecture on the Android side.
### Full System Flow

```text
Python Scraper
    ↓
PostgreSQL Database
    ↓
Go Backend API
    ↓
Android App (MVVM)
```

### Android MVVM architecture
```text 
UI (Activity) (View)
    ↓
ViewModel
    ↓
Repository (Model)
    ↓
Retrofit API (Model)
    ↓
Go Backend
```
___

## Project Structure
```text
app/                    → Android application
lawsuit-backend/        → Go backend API
lawsuit-scraper/        → Python scraping scripts

app/src/main/java/com/example/lawsuitapp/
│
├── data/
│   ├── model/
│   ├── remote/
│   └── repository/
│
└── ui/
    ├── main/
    └── detail/
```
___

## Technologies Used
- **Android / Kotlin**
- **MVVM Architecture**
- **REST API**
- **Go**
- **Python**
- **beautifulSoup**
- **PostgreSQL**
- **Retrofit**
- **Git & GitHub**
___

## How it works
	1.	The Python scraper collects lawsuit information from source websites.
	2.	Scraped data is stored in PostgreSQL.
	3.	The Go backend reads data from PostgreSQL and exposes REST API endpoints.
	4.	The Android application fetches data using Retrofit.
	5.	The app displays lawsuit information using MVVM architecture.
___

## Running the Project

### 1. Start PostgreSQL
Make sure PostgreSQL run locally.
You can test db_connection with:
```
psql "postgres://lawsuit_app:lawsuit@localhost:5432/lawsuit_db?sslmode=disable"
```
Example query:
```
SELECT COUNT(*) FROM lawsuits;
```
Exit PostgreSQL:
```
\q
```

### 2. Run Backend API
Go to the backend folder:
```text
cd lawsuit_backend
```
Export the database connection string:
```bash
export DATABASE_URL="postgres://lawsuit_app:lawsuit@localhost:5432/lawsuit_db?sslmode=disable"
```
Run the Go backend:
```text
go run main.go
```
The backend runs locally on:
```text
http://localhost:8080
```
You can test the API in a browser:
```text
http://localhost:8080/lawsuits
```

### 3. Run Android App
Open the project in Android Studio and run the Android app using an emulator.

The Android emulator connects to the local backend through:
```text
http://10.0.2.2:8080/
```

This URL is configured in:

```text
app/src/main/java/com/example/lawsuitapp/data/remote/RetrofitInstance.kt
```

