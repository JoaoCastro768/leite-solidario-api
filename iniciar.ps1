param([int]$TimeoutSeconds = 180)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

function Get-DockerEngine {
    try {
        $engine = & docker info --format '{{.OSType}}' 2>$null
        if ($LASTEXITCODE -eq 0) { return ([string]$engine).Trim() }
    } catch {}
    return $null
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker não encontrado. Instale e abra o Docker Desktop antes de continuar."
}

$engine = Get-DockerEngine
if (-not $engine) {
    Write-Host "Iniciando o Docker Desktop..."
    $started = $false
    try {
        & docker desktop start --detach 2>$null
        $started = $LASTEXITCODE -eq 0
    } catch {}
    if (-not $started) {
        $candidates = @(
            "$env:ProgramFiles\Docker\Docker\Docker Desktop.exe",
            "$env:LOCALAPPDATA\Programs\DockerDesktop\Docker Desktop.exe"
        )
        foreach ($candidate in $candidates) {
            if (Test-Path $candidate) {
                Start-Process -FilePath $candidate
                $started = $true
                break
            }
        }
    }
    if (-not $started) {
        throw "Não foi possível abrir o Docker Desktop. Abra o aplicativo pelo menu Iniciar e confira a mensagem exibida."
    }
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $engine = Get-DockerEngine
        if ($engine) { break }
        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $deadline)
    if (-not $engine) {
        throw "O Docker não iniciou no prazo. Confira na janela do Docker Desktop se existe um erro de WSL ou virtualização."
    }
}
if ($engine -ne 'linux') {
    throw "O Docker está no modo Windows. No menu do Docker Desktop, selecione Switch to Linux containers e execute novamente."
}

if (-not (Test-Path ".env")) { Copy-Item ".env.example" ".env" }
& docker compose config --quiet
if ($LASTEXITCODE -ne 0) { throw "Configuração inválida. Confira compose.yml e .env." }
& docker compose up -d --build
if ($LASTEXITCODE -ne 0) { throw "Falha ao iniciar os serviços. Confira: docker compose logs sqlserver db-init api" }

$port = '9000'
foreach ($line in Get-Content .env) {
    if ($line -match '^\s*API_PORT\s*=\s*(\d+)\s*$') { $port = $Matches[1] }
}
$url = "http://localhost:$port"
$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
Write-Host "Aguardando a API e o SQL Server..."
do {
    try {
        $health = Invoke-RestMethod "$url/actuator/health" -TimeoutSec 5
        if ($health.status -eq 'UP') {
            Write-Host "API e SQL Server disponíveis. Swagger: $url"
            exit 0
        }
    } catch {}
    Start-Sleep -Seconds 3
} while ((Get-Date) -lt $deadline)
throw "A API não ficou disponível no prazo. Confira: docker compose logs api sqlserver db-init"
