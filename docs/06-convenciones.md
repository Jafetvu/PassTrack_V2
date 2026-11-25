# CONVENCIONES · Testing, Arquitectura y CI/CD (PassTrack)

## 1. Decisiones de Proceso y Arquitectura

| \# | Decisión | Alternativa descartada | Por qué | Fecha |
| :--- | :--- | :--- | :--- | :--- |
| **C-01** | Arquitectura Multicapa en `PassTrack_backend` (Controlador, Servicio, Repositorio, Modelo). | Arquitectura Hexagonal. | Reduce la complejidad inicial y mapea de forma natural con los estándares de Spring Boot. | 2026-11-06 |
| **C-02** | Frontend Basado en Funcionalidades (Feature-Based) con patrón Contenedor/Presentacional. | Agrupar por tipo (todas las vistas juntas, todos los hooks juntos). | Escala mejor al aislar el dominio visual y lógico de cada módulo (auth, asistencias, justificantes). | 2026-11-06 |
| **C-03** | Flujo de ramas por entorno: `main` (estructura inicial), `develop` (desarrollo + ramas de una sola funcionalidad) y promoción por PR hacia `test` y `prod` para Jenkins. | Trunk-Based directo sobre `main`. | Mantiene intacta la base inicial en `main`, aísla el desarrollo diario en `develop` y obliga a validar cada entrega en el pipeline de `test` antes de pasar a `prod`. | 2026-11-25 |
| **C-04** | Java Records obligatorios como DTOs en Controllers. | Recibir `@Entity` JPA. | Previene Mass Assignment (ej. inyección de roles no autorizados) y garantiza inmutabilidad. | 2026-11-06 |
| **C-05** | `./scripts/check.sh` unificado para local y Jenkins. | Scripts aislados. | Paridad de entornos: si el test pasa en tu computadora, pasará en el pipeline. | 2026-11-06 |

## 2. Organización del Repositorio

```text
PassTrack/
├── PassTrack_backend/
│   └── src/main/java/com/passtrack/api/
│       ├── controller/      # Endpoints REST (@RestController)
│       ├── service/         # Reglas de negocio (@Service)
│       ├── repository/      # Interfaces Spring Data JPA
│       ├── model/           # Entidades JPA (@Entity)
│       ├── dto/             # Java Records (Request/Response)
│       └── security/        # Filtros JWT y configuraciones
├── PassTrack_frontend/
│   └── src/
│       ├── features/
│       │   ├── auth/            # Dominio de autenticación
│       │   ├── asistencias/     # Componentes, vistas y lógica de pase de lista
│       │   └── justificantes/   # Gestión de comprobantes
│       ├── context/         # Estados globales (AuthContext)
│       └── api/             # Cliente Axios con Interceptores
└── docs/
```

## 3. Patrones de Diseño Seleccionados

### Backend (Spring Boot)
1. **Repository Pattern:** Aislamiento de consultas SQL en JPA (`repository/`) para facilitar el testing de los servicios mockeando la base de datos.
2. **DTO (Data Transfer Object):** Contratos inmutables de API REST usando `Java Records` en la capa `controller/`.
3. **Chain of Responsibility:** El `JwtAuthenticationFilter` procesa la petición HTTP, valida la firma criptográfica y establece el `SecurityContext` antes de que alcance los controladores.
4. **Dependency Injection:** Uso del contenedor IoC de Spring para desacoplar las capas lógicas (Controlador inyecta Servicio, Servicio inyecta Repositorio).

### Frontend (React)
1. **Contenedor / Presentacional (Smart & Dumb Components):** Los componentes contenedores (ej. `ListaAsistenciaContainer`) hacen llamadas a la API y manejan estados, pasando datos limpios vía *props* a componentes presentacionales (ej. `FilaAlumnoItem`) que se limitan a renderizar la UI.
2. **Provider Pattern:** Uso de Context API (`AuthContext`) para proveer el estado de la sesión y el rol decodificado a toda la aplicación sin requerir *prop drilling*.
3. **Interceptor Pattern:** Cliente Axios centralizado que inyecta automáticamente el token JWT en las cabeceras salientes y captura códigos `401`/`403` para redirigir al login.

## 4. Estrategia de Testing (Pipeline Level)

- **Backend:** JUnit 5 + MockMvc. Pruebas obligatorias de RBAC (acceder con rol equivocado espera `403`).
- **Frontend:** ESLint estricto para análisis estático de código.
- **Regla de oro:** Todo bug corregido = Nuevo test añadido para evitar regresiones futuras.

## 5. Script Sensor (Nivel AU)

Archivo `./scripts/check.sh` bloqueante:

```bash
#!/usr/bin/env bash
set -e
echo "==> [1/2] Tests Backend..."
(cd PassTrack_backend && ./mvnw test -B)
echo "==> [2/2] Build Frontend..."
(cd PassTrack_frontend && npm run lint && npm run build)
echo "==> Listo para commit/deploy."
```

## 6. Flujo de Ramas y Promoción CI/CD (Jenkins)

1. **`main` (Base inicial):** Almacena únicamente la estructura inicial del monorepo; no se realizan commits de funcionalidades ni despliegues sobre esta rama.
2. **`develop` y ramas por funcionalidad:** Todo el desarrollo vive en `develop`. Cada nueva característica o corrección se trabaja en una rama dedicada de una sola funcionalidad (`feat/<funcionalidad>` o `fix/<correccion>`) que nace de `develop` y vuelve mediante merge a `develop` tras pasar `./scripts/check.sh`.
3. **Promoción a `test` (PR `develop` → `test`):** Para ejecutar las validaciones de integración continua, se abre un Pull Request desde `develop` hacia `test`, lo cual dispara el pipeline de pruebas en Jenkins (`./mvnw test`, linter y verificación de build/contenedores).
4. **Promoción a `prod` (PR `test` → `prod`):** Solo cuando el pipeline de `test` concluye sin errores, se abre un Pull Request desde `test` hacia `prod` para que Jenkins ejecute el pipeline de producción, construya las imágenes finales y actualice los contenedores mediante Docker Compose.