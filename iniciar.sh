#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if ! command -v docker >/dev/null 2>&1; then
    printf '%s\n' 'Docker nao encontrado. Instale e inicie o Docker.' >&2
    exit 1
fi
engine=$(docker info --format '{{.OSType}}' 2>/dev/null) || {
    printf '%s\n' 'O Docker nao esta em execucao. Inicie o Docker e tente novamente.' >&2
    exit 1
}
if [ "$engine" != 'linux' ]; then
    printf '%s\n' 'Selecione Linux containers no Docker Desktop.' >&2
    exit 1
fi
if [ ! -f .env ]; then cp .env.example .env; fi
docker compose config --quiet
docker compose up -d --build
port=$(sed -n 's/^API_PORT=\([0-9][0-9]*\)$/\1/p' .env | tail -n 1)
port=${port:-9000}
printf '%s\n' "Swagger: http://localhost:$port" 'Acompanhe: docker compose logs -f api'
