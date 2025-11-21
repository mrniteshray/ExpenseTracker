# Expense Tracker Backend

Simple FastAPI backend for expense tracking with Firebase Authentication and Firestore.

## Features

- User authentication (signup/login) using Firebase Auth
- Expense CRUD operations stored in Firestore
- Dashboard with category-wise expense summaries
- Simple REST API for Android app communication
- No complex middleware or JWT tokens

## Tech Stack

- **Framework**: FastAPI
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth (via Firebase Admin SDK)
- **Validation**: Pydantic
- **Server**: Uvicorn

## Setup

### 1. Install Dependencies

```bash
cd ExpenseTrackerBackend
pip install -r requirements.txt
```

### 2. Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing one
3. Enable Firestore Database
4. Go to Project Settings → Service Accounts
5. Click "Generate New Private Key"
6. Save the JSON file as `serviceAccountKey.json` in the ExpenseTrackerBackend directory

Or set environment variable:
```bash
export FIREBASE_CREDENTIALS_PATH=/path/to/serviceAccountKey.json
```

### 3. Run the Server

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

The API will be available at: `http://localhost:8000`

## API Documentation

Once running, access interactive docs at:
- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

## API Endpoints

### Authentication

**Signup**
```
POST /auth/signup
Body: {"email": "user@example.com", "password": "password123"}
Response: {"uid": "...", "email": "...", "id_token": "..."}
```

**Login**
```
POST /auth/login
Body: {"email": "user@example.com", "password": "password123"}
Response: {"uid": "...", "email": "...", "id_token": "..."}
```

### Expenses

All expense endpoints require `user_id` query parameter.

**Create Expense**
```
POST /expenses
Body: {
  "user_id": "firebase-user-id",
  "amount": 100.50,
  "category": "Food",
  "description": "Lunch at restaurant",
  "date": "2024-01-15T12:00:00"
}
```

**List Expenses**
```
GET /expenses?user_id=firebase-user-id
GET /expenses?user_id=...&category=Food
GET /expenses?user_id=...&start_date=2024-01-01T00:00:00&end_date=2024-01-31T23:59:59
```

**Get Single Expense**
```
GET /expenses/{expense_id}?user_id=firebase-user-id
```

**Update Expense**
```
PUT /expenses/{expense_id}?user_id=firebase-user-id
Body: {
  "amount": 120.00,
  "description": "Updated description"
}
```

**Delete Expense**
```
DELETE /expenses/{expense_id}?user_id=firebase-user-id
```

### Dashboard

**Get Summary**
```
GET /dashboard/summary?user_id=firebase-user-id
Response: {
  "overall_total": 1000.50,
  "total_count": 25,
  "per_category": [
    {"category": "Food", "total_amount": 450.75, "count": 15},
    {"category": "Transport", "total_amount": 300.25, "count": 10}
  ]
}
```

## Project Structure

```
ExpenseTrackerBackend/
├── app/
│   └── main.py              # Single file with all routes and logic
├── requirements.txt         # Python dependencies
├── serviceAccountKey.json   # Firebase credentials (not in git)
└── README.md               # This file
```

## Architecture Design

This is a **simplified** architecture where:
- **Backend** handles ALL Firebase operations (Auth + Firestore)
- **Android app** only makes REST API calls via Retrofit
- No Firebase SDK on Android side
- Authentication done via `/auth/signup` and `/auth/login` endpoints
- User ID passed as query parameter (no JWT middleware)

## Data Model

### Expense Document (Firestore)
```javascript
{
  "user_id": "firebase-uid",
  "amount": 100.50,
  "category": "Food",
  "description": "Lunch",
  "date": "2024-01-15T12:00:00",
  "created_at": "2024-01-15T12:00:00",
  "updated_at": "2024-01-15T12:00:00"
}
```

## Security Notes

- User authentication handled by Firebase Auth
- Each expense operation verifies user ownership via `user_id`
- All expense operations check that the requesting user owns the expense
- CORS enabled for all origins (configure as needed for production)

## Error Responses

```json
{
  "detail": "Error message here"
}
```

Common status codes:
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `404` - Not Found
- `500` - Internal Server Error

## Testing

```bash
# Health check
curl http://localhost:8000/

# Create user
curl -X POST http://localhost:8000/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'

# Login
curl -X POST http://localhost:8000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'
```

## Production Deployment

For production, consider:
- Setting specific CORS origins
- Using environment variables for credentials
- Adding proper logging
- Setting up monitoring
- Using a reverse proxy (Nginx)
- Enabling HTTPS

## Dependencies

```
fastapi==0.109.0
uvicorn[standard]==0.27.0
pydantic==2.5.3
firebase-admin==6.4.0
python-multipart==0.0.6
```

## License

This project is part of an assignment.

