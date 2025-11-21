#!/bin/bash
# Simple startup script

echo "Starting Backend..."
cd app
uvicorn main:app --reload --host 0.0.0.0 --port 8000
