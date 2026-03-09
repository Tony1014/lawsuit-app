# Lawsuit Information App (Canada)

This project is a simple full-stack application that collects and displays information about class action lawsuits in BC.

It is built for learning purposes and helps users view lawsuit information such as:
- whether compensation may be available
- whether claim is required
- whether proof is required
- the difficulty of applying
- the source link for more details

## Project Structure

- `app/` — Android app in Kotlin
- `lawsuit-backend/` — Go backend API
- `lawsuit-scraper/` — Python scraper
- `gradle/` and Gradle files — Android build system

## Technologies Used

- **Android / Kotlin**
- **Go**
- **Python**
- **PostgreSQL**
- **Retrofit**
- **GitHub**

## How It Works

1. The Python scraper collects lawsuit information from source websites.
2. The data is stored in PostgreSQL.
3. The Go backend reads data from the database and provides API endpoints.
4. The Android app fetches data from the backend and displays it to the user.

## Features

- View lawsuit titles
- Filter by claim-related information
- Check whether proof is required
- View difficulty level
- Open source URL for more details

## Running the Project

### 1. Backend
Go to the backend folder:

```bash
cd lawsuit-backend
go run main.go