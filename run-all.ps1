# Load local .env file variables
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -match '^\s*$') { return }
    $key, $value = $_ -split '=', 2
    Set-Item -Path "Env:$($key.Trim())" -Value $value.Trim()
}

$env:DB_PORT = $env:POSTGRES_PORT
$env:DB_USER = $env:POSTGRES_USER
$env:DB_PASSWORD = $env:POSTGRES_PASSWORD

Write-Host "Building all projects first..." -ForegroundColor Green
.\mvnw.cmd clean install -DskipTests

Write-Host "Launching services in separate windows..." -ForegroundColor Cyan

$services = @("user-service", "account-service", "transaction-service", "logging-service", "bff-service")

foreach ($service in $services) {
    Write-Host "Starting $service..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$Host.UI.RawUI.WindowTitle='$service'; .\run-service.ps1 -Module $service"
}

Write-Host "All services started!" -ForegroundColor Green
