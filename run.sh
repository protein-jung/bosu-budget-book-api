#!/usr/bin/env bash
# .env에 있는 값(MOLIT_API_KEY 등)을 환경변수로 불러온 뒤 bootRun을 실행한다.
set -a
[ -f "$(dirname "$0")/.env" ] && source "$(dirname "$0")/.env"
set +a

cd "$(dirname "$0")"
./gradlew bootRun
