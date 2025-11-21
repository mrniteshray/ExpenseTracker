@echo off
REM Simple startup script for Windows

echo Starting Backend...
cd app
uvicorn main:app --reload --host 0.0.0.0 --port 8000
