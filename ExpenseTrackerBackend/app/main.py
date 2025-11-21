"""
Expense Tracker Backend API
Simple FastAPI application with Firebase Authentication and Firestore
"""
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime
import firebase_admin
from firebase_admin import credentials, auth, firestore
import os

# Initialize Firebase
if not firebase_admin._apps:
    # Try multiple paths for the credentials file
    cred_path = os.getenv("FIREBASE_CREDENTIALS_PATH")
    
    if not cred_path:
        # Try different possible locations
        possible_paths = [
            "/etc/secrets/serviceAccountKey.json",  # Render secret file location
            "./serviceAccountKey.json",              # Local development
            "../serviceAccountKey.json",             # One level up
            "ExpenseTrackerBackend/serviceAccountKey.json"  # From root
        ]
        for path in possible_paths:
            if os.path.exists(path):
                cred_path = path
                break
    
    if cred_path and os.path.exists(cred_path):
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred)
    else:
        # This will fail and show error message
        raise FileNotFoundError(
            "Firebase credentials not found! "
            "Please upload serviceAccountKey.json as a Secret File in Render. "
            f"Tried paths: {possible_paths}"
        )

db = firestore.client()

# Initialize FastAPI app
app = FastAPI(title="Expense Tracker API", version="1.0.0")

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============= Models =============

class SignUpRequest(BaseModel):
    email: str
    password: str

class LoginRequest(BaseModel):
    email: str
    password: str

class AuthResponse(BaseModel):
    uid: str
    email: str
    id_token: str

class ExpenseRequest(BaseModel):
    amount: float = Field(gt=0)
    description: str = Field(min_length=1, max_length=500)
    date: str  # ISO format
    category: str
    user_id: str

class ExpenseUpdateRequest(BaseModel):
    amount: Optional[float] = Field(None, gt=0)
    description: Optional[str] = Field(None, min_length=1, max_length=500)
    date: Optional[str] = None
    category: Optional[str] = None

class Expense(BaseModel):
    id: str
    amount: float
    description: str
    date: str
    category: str
    user_id: str
    created_at: Optional[str] = None
    updated_at: Optional[str] = None

class CategorySummary(BaseModel):
    category: str
    total_amount: float
    count: int

class DashboardSummary(BaseModel):
    overall_total: float
    total_count: int
    per_category: List[CategorySummary]

# ============= Endpoints =============

@app.get("/")
async def root():
    return {"message": "Expense Tracker API is running", "version": "1.0.0"}

@app.get("/health")
async def health_check():
    return {"status": "healthy", "timestamp": datetime.utcnow().isoformat()}


# ============= Authentication Endpoints =============

@app.post("/auth/signup", response_model=AuthResponse)
async def signup(request: SignUpRequest):
    """Create new user with email and password"""
    try:
        user = auth.create_user(
            email=request.email,
            password=request.password
        )
        # Create custom token for the user
        custom_token = auth.create_custom_token(user.uid)
        return AuthResponse(
            uid=user.uid,
            email=user.email,
            id_token=custom_token.decode('utf-8')
        )
    except auth.EmailAlreadyExistsError:
        raise HTTPException(status_code=400, detail="Email already exists")
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/auth/login", response_model=AuthResponse)
async def login(request: LoginRequest):
    """Login user - returns user info (client handles Firebase Auth)"""
    try:
        user = auth.get_user_by_email(request.email)
        # Create custom token
        custom_token = auth.create_custom_token(user.uid)
        return AuthResponse(
            uid=user.uid,
            email=user.email,
            id_token=custom_token.decode('utf-8')
        )
    except auth.UserNotFoundError:
        raise HTTPException(status_code=404, detail="User not found")
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

# ============= Expense Endpoints =============

@app.post("/expenses", response_model=Expense, status_code=201)
async def create_expense(expense: ExpenseRequest):
    """Create a new expense"""
    try:
        expense_data = expense.dict()
        expense_data['created_at'] = datetime.utcnow().isoformat()
        expense_data['updated_at'] = datetime.utcnow().isoformat()
        
        doc_ref = db.collection('expenses').document()
        doc_ref.set(expense_data)
        
        return Expense(id=doc_ref.id, **expense.dict())
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to create expense: {str(e)}")

@app.get("/expenses/{expense_id}", response_model=Expense)
async def get_expense(expense_id: str, user_id: str):
    """Get a specific expense"""
    try:
        doc = db.collection('expenses').document(expense_id).get()
        if not doc.exists:
            raise HTTPException(status_code=404, detail="Expense not found")
        
        data = doc.to_dict()
        if data.get('user_id') != user_id:
            raise HTTPException(status_code=403, detail="Not authorized")
        
        return Expense(id=doc.id, **data)
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.put("/expenses/{expense_id}", response_model=Expense)
async def update_expense(expense_id: str, expense: ExpenseUpdateRequest, user_id: str):
    """Update an expense"""
    try:
        doc_ref = db.collection('expenses').document(expense_id)
        doc = doc_ref.get()
        
        if not doc.exists:
            raise HTTPException(status_code=404, detail="Expense not found")
        
        data = doc.to_dict()
        if data.get('user_id') != user_id:
            raise HTTPException(status_code=403, detail="Not authorized")
        
        update_data = {k: v for k, v in expense.dict().items() if v is not None}
        update_data['updated_at'] = datetime.utcnow().isoformat()
        
        doc_ref.update(update_data)
        
        updated_doc = doc_ref.get()
        updated_data = updated_doc.to_dict()
        return Expense(id=updated_doc.id, **updated_data)
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/expenses/{expense_id}")
async def delete_expense(expense_id: str, user_id: str):
    """Delete an expense"""
    try:
        doc_ref = db.collection('expenses').document(expense_id)
        doc = doc_ref.get()
        
        if not doc.exists:
            raise HTTPException(status_code=404, detail="Expense not found")
        
        data = doc.to_dict()
        if data.get('user_id') != user_id:
            raise HTTPException(status_code=403, detail="Not authorized")
        
        doc_ref.delete()
        return {"success": True, "message": "Expense deleted"}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/expenses", response_model=List[Expense])
async def list_expenses(
    user_id: str,
    category: Optional[str] = None,
    start_date: Optional[str] = None,
    end_date: Optional[str] = None
):
    """List expenses with optional filters"""
    try:
        query = db.collection('expenses').where('user_id', '==', user_id)
        
        if category:
            query = query.where('category', '==', category)
        
        docs = query.stream()
        expenses = []
        
        for doc in docs:
            data = doc.to_dict()
            expense = Expense(id=doc.id, **data)
            
            # Filter by date if provided
            if start_date and expense.date < start_date:
                continue
            if end_date and expense.date > end_date:
                continue
            
            expenses.append(expense)
        
        # Sort by date descending
        expenses.sort(key=lambda x: x.date, reverse=True)
        return expenses
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/dashboard/summary", response_model=DashboardSummary)
async def get_dashboard_summary(user_id: str):
    """Get dashboard summary"""
    try:
        query = db.collection('expenses').where('user_id', '==', user_id)
        docs = query.stream()
        
        category_data = {}
        overall_total = 0.0
        total_count = 0
        
        for doc in docs:
            data = doc.to_dict()
            category = data['category']
            amount = data['amount']
            
            if category not in category_data:
                category_data[category] = {'total_amount': 0.0, 'count': 0}
            
            category_data[category]['total_amount'] += amount
            category_data[category]['count'] += 1
            overall_total += amount
            total_count += 1
        
        per_category = [
            CategorySummary(
                category=cat,
                total_amount=round(data['total_amount'], 2),
                count=data['count']
            )
            for cat, data in category_data.items()
        ]
        
        per_category.sort(key=lambda x: x.total_amount, reverse=True)
        
        return DashboardSummary(
            overall_total=round(overall_total, 2),
            total_count=total_count,
            per_category=per_category
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
