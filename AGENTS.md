# AGENTS.md · Reglas Operativas para Agentes (PassTrack Monorepo)

1. [SENSOR] Ejecutar `./mvnw test` en backend y `npm run lint` en frontend antes de realizar cualquier commit o PR.
2. [SENSOR] En `PassTrack_backend` se prohíbe saltar capas: el Controlador solo debe invocar al Servicio; jamás debe llamar directamente al Repositorio sin pasar por la capa de Servicio.
3. [GUÍA] En `PassTrack_frontend` organizar el código por features (`src/features/<modulo>/`) y aplicar el patrón Smart/Dumb Components (Contenedores gestionan estado/API; Dumb solo renderizan props).
4. [GUÍA] Endpoints REST de Spring Boot deben recibir y responder exclusivamente DTOs inmutables con Java Records; nunca exponer ni recibir entidades `@Entity` JPA directamente.
5. [GUÍA] Estado global limitado a `AuthContext` (Provider Pattern) y peticiones HTTP vía Axios con Interceptores JWT; no duplicar estado ni crear dependencias superfluas.
6. [GUÍA] Implementar doble barrera de seguridad: `@PreAuthorize` por rol en controladores y validación de propiedad/titularidad de datos en la capa Servicio (esperar 403 en RBAC fallido).
7. [GUÍA] Mantener paridad absoluta entre entornos locales y CI/CD ejecutando las validaciones mediante el script sensor unificado `./scripts/check.sh`.