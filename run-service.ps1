param(
    [Parameter(Mandatory=$true)]
    [string]$Module
)

Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -match '^\s*$') { return }
    $key, $value = $_ -split '=', 2
    Set-Item -Path "Env:$($key.Trim())" -Value $value.Trim()
}

$env:DB_PORT = $env:POSTGRES_PORT
$env:DB_USER = $env:POSTGRES_USER
$env:DB_PASSWORD = $env:POSTGRES_PASSWORD

.\mvnw.cmd -pl $Module -am install -DskipTests
.\mvnw.cmd -pl $Module spring-boot:run