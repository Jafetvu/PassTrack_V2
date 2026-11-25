# PassTrack Backend API

API REST construida con **Spring Boot 3.2 (Java 21)** siguiendo una **Arquitectura Multicapa** estricta (`controller` -> `service` -> `repository` -> `model`).

## Estructura de Paquetes (`com.passtrack.api`)

- `config/` — Configuraciones globales y CORS.
- `controller/` — Controladores REST protegidos con `@PreAuthorize` (solo reciben/responden DTOs Java Records).
- `dto/` — Contratos inmutables de entrada/salida con Java Records.
- `exception/` — Manejador global de excepciones (`@RestControllerAdvice`).
- `model/` — Entidades JPA (`@Entity`).
- `repository/` — Interfaces Spring Data JPA.
- `security/` — `JwtAuthenticationFilter`, `JwtService` y `SecurityFilterChain`.
- `service/` — Lógica de negocio, transacciones y validación de titularidad (Ownership).

## Validación Local

```bash
./mvnw test -B
```
