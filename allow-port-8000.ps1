# Run this as Administrator to allow port 8000 through Windows Firewall

Write-Host "Adding firewall rule for port 8000..." -ForegroundColor Green

New-NetFirewallRule -DisplayName "FastAPI Backend Port 8000" `
    -Direction Inbound `
    -Protocol TCP `
    -LocalPort 8000 `
    -Action Allow `
    -Profile Any

Write-Host "Firewall rule added successfully!" -ForegroundColor Green
Write-Host "Your backend at http://192.168.1.3:8000 should now be accessible from your phone" -ForegroundColor Cyan
