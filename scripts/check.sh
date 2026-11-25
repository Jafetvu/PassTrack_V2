#!/usr/bin/env bash
set -e

echo "==> [1/2] Tests Backend..."
(cd PassTrack_backend && ./mvnw test -B)

echo "==> [2/2] Lint y Build Frontend..."
(cd PassTrack_frontend && npm run lint && npm run build)

echo "==> Validación integral completada. Listo para commit/deploy."
