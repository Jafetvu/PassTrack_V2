# PassTrack · Sistema de Control Escolar de Asistencias y Justificantes

PassTrack es una plataforma web integral diseñada para la gestión operativa de listas de asistencia universitaria y dictamen de justificantes médicos digitales. El sistema implementa una arquitectura desacoplada basada en microservicios contenerizados, control de acceso de mínimo privilegio mediante tokens JWT y un flujo continuo de integración y despliegue (CI/CD) automatizado con Jenkins y Docker.

---

## 1. Información General y Autoría

- **Proyecto:** PassTrack
- **Desarrollador Full-Stack & DevOps:** Jafet Bahena González
- **Metodología de Desarrollo:** Spec-Driven Development (SDD) & Flujo de Ramas por Entorno (`develop` → `test` → `prod`)
- **Arquitectura de Software:** Monorepo con Backend REST, Frontend SPA y Base de Datos Relacional contenerizada bajo red privada virtual bridge.

---

## 2. Stack Tecnológico

| Capa / Servicio | Tecnología | Versión | Propósito |
| :--- | :--- | :--- | :--- |
| **Backend API** | Java / Spring Boot | 3.2.x (Java 21) | Lógica de negocio, seguridad transaccional y endpoints REST. |
| **Seguridad API** | Spring Security + JJWT | 0.12.x | Filtro stateless, hashing BCrypt y validación de claims JWT. |
| **Persistencia** | Spring Data JPA / Hibernate | 3.2.x | Mapeo objeto-relacional y consultas optimizadas. |
| **Frontend Web** | React + Vite | 18.x | Interfaz SPA responsiva, consumo HTTP vía Axios e interceptores. |
| **Servidor Web Front** | Nginx | 1.25 Alpine | Servidor de archivos estáticos y proxy inverso para `/api/*`. |
| **Base de Datos** | MySQL Server | 8.0 | Motor relacional con llaves compuestas y restricciones transaccionales. |
| **CI / CD Pipeline** | Jenkins | LTS | Orquestación automatizada de pruebas, compilación y despliegue. |
| **Contenedores** | Docker & Docker Compose | Engine 24+ | Empaquetado multi-stage y aislamiento de redes entre servicios. |

---

## 3. Matriz de Mínimo Privilegio y Seguridad (RBAC + Ownership)

El sistema opera bajo el principio de **Mínimo Privilegio**: ningún usuario puede leer, modificar o crear registros fuera de su rol estricto o de su ámbito de pertenencia. La seguridad se evalúa en dos niveles: mediante anotaciones `@PreAuthorize` en los controladores y validación de propiedad (*Ownership*) en la capa `Service`.

### Roles del Sistema
1. **`ROLE_DIRECTOR`:** Administrador académico. Control total sobre usuarios, creación de grupos y aprobación final de justificantes médicos.
2. **`ROLE_DOCENTE`:** Profesor de grupo. Solo puede consultar los grupos que tiene asignados y registrar asistencia del día en curso (`LocalDate.now()`). No puede alterar registros pasados ni ver grupos ajenos.
3. **`ROLE_ALUMNO`:** Estudiante. Solo puede visualizar su propio porcentaje de asistencia e historial, y enviar solicitudes de justificante asociadas a sus faltas.

### Matriz de Endpoints y Códigos HTTP

| Endpoint | Método | Roles Permitidos | Regla de Negocio / Verificación de Propiedad | Respuesta Esperada | No Autorizado |
| :--- | :---: | :---: | :--- | :---: | :---: |
| `/api/auth/login` | `POST` | Público | Autentica email y password; retorna JWT con claim `rol` y `userId`. | `200 OK` | `401 Unauthorized` |
| `/api/usuarios` | `POST` | `DIRECTOR` | Crea docentes o alumnos; encripta password con BCrypt (costo 10). | `201 Created` | `403 Forbidden` |
| `/api/usuarios` | `GET` | `DIRECTOR` | Lista el directorio institucional de usuarios activos. | `200 OK` | `403 Forbidden` |
| `/api/grupos` | `POST` | `DIRECTOR` | Registra una nueva materia y le asigna un docente titular. | `201 Created` | `403 Forbidden` |
| `/api/grupos/docente` | `GET` | `DOCENTE` | Retorna únicamente los grupos donde `docente_id == token.userId`. | `200 OK` | `403 Forbidden` |
| `/api/asistencias/grupo/{id}` | `POST` | `DOCENTE` | Registra lista de hoy. Valida que el docente sea titular del grupo. | `201 Created` | `403 / 409 Conflict` |
| `/api/asistencias/mis-asistencias` | `GET` | `ALUMNO` | Consulta registros donde `alumno_id == token.userId`. | `200 OK` | `403 Forbidden` |
| `/api/justificantes` | `POST` | `ALUMNO` | Solicita justificación de una falta propia (`asistencia.alumno.id == token.userId`). | `201 Created` | `403 Forbidden` |
| `/api/justificantes` | `GET` | `DIRECTOR` | Consulta la bandeja global de solicitudes pendientes. | `200 OK` | `403 Forbidden` |
| `/api/justificantes/{id}/estado` | `PATCH` | `DIRECTOR` | Dictamina estado (`APROBADO`/`RECHAZADO`). Si aprueba, muta falta a `JUSTIFICADO`. | `200 OK` | `403 Forbidden` |

---

## 4. Estructura del Monorepo

```text
PassTrack/
├── PassTrack_backend/             # API REST en Java Spring Boot
│   ├── src/
│   │   ├── main/java/com/passtrack/api/
│   │   │   ├── config/            # Beans globales y CORS
│   │   │   ├── controller/        # Controladores REST protegidos con @PreAuthorize
│   │   │   ├── dto/               # Java Records inmutables (Request / Response)
│   │   │   ├── exception/         # Manejador global de excepciones (@RestControllerAdvice)
│   │   │   ├── model/             # Entidades JPA (Usuario, Grupo, Asistencia, Justificante)
│   │   │   ├── repository/        # Repositorios Spring Data JPA
│   │   │   ├── security/          # JwtAuthenticationFilter, JwtService, SecurityFilterChain
│   │   │   └── service/           # Lógica de negocio y validación de titularidad
│   │   └── test/java/com/passtrack/api/
│   │       ├── security/          # Pruebas de mínimo privilegio (403 Forbidden con MockMvc)
│   │       └── service/           # Pruebas unitarias de negocio (JUnit 5 + Mockito)
│   ├── Dockerfile                 # Multi-stage build (Maven -> Eclipse Temurin JRE 21)
│   ├── pom.xml                    # Configuración de dependencias Maven
│   └── README.md                  # Especificación técnica del backend
├── PassTrack_frontend/            # Aplicación Cliente SPA (React + Vite)
│   ├── src/
│   │   ├── api/                   # Cliente Axios centralizado con interceptor de JWT
│   │   ├── components/            # Layouts, tablas y componentes de protección de rutas
│   │   ├── context/               # AuthContext (almacenamiento y decodificación del JWT)
│   │   └── pages/                 # Vistas agrupadas por rol (director, docente, alumno)
│   ├── Dockerfile                 # Multi-stage build (Node build -> Nginx Alpine)
│   ├── nginx.conf                 # Configuración de proxy inverso hacia backend:8080
│   └── package.json               # Dependencias de npm y scripts de ejecución
├── dev-cli/                       # CLI interactivo DX en Node.js (Inquirer, Chalk, Figlet)
│   ├── dev.mjs                    # Panel interactivo con multiplexacion de logs
│   └── package.json               # Dependencias de terminal interactiva
├── docs/                          # Especificaciones Spec-Driven Development
│   ├── 01-alcance.md              # SOW (Objetivos, fases, riesgos y decisiones D-01 a D-07)
│   ├── 02-prd.md                  # PRD (Historias de usuario Dado/Cuando/Entonces y NFRs)
│   ├── 03-integraciones.md        # Integraciones con MySQL, almacenamiento y Jenkins Webhooks
│   ├── 04-arquitectura.md         # Topología de red, composición de microservicios y seguridad
│   ├── 05-datos.md                # ERD conceptual en Mermaid, diccionarios y restricciones
│   └── 06-convenciones.md         # Estándares de testing, Conventional Commits y CI/CD
├── scripts/
│   └── check.sh                   # Script de verificación integral (tests backend + build frontend)
├── .env.example                   # Plantilla de variables de entorno seguras
├── .gitignore                     # Exclusión de binarios, .env y llaves de acceso
├── AGENTS.md                      # Reglas operativas estrictas para el monorepo (máx. 20 líneas)
├── docker-compose.yml             # Orquestación de contenedores (db, backend, frontend)
├── Jenkinsfile                    # Pipeline declarativo automatizado para CI/CD
├── start.bat                      # Lanzador universal para Windows PowerShell/CMD
├── start.sh                       # Script de arranque concurrente DX (Spring Boot + Vite)
└── README.md                      # Documentación maestra del repositorio
```

---

## 5. Arquitectura de Despliegue en Contenedores

La solución se ejecuta sobre una red interna privada de Docker (`passtrack_net`), asegurando que la base de datos MySQL no exponga su puerto directamente al exterior en producción:

```text
               PUERTO 80 (HTTP)
                      │
                      ▼
        ┌───────────────────────────┐
        │   PassTrack_frontend      │
        │   (Nginx 1.25 Alpine)     │
        │   - Sirve bundle React    │
        │   - Proxy /api/*          │
        └─────────────┬─────────────┘
                      │ http://backend:8080
                      ▼
        ┌───────────────────────────┐
        │    PassTrack_backend      │
        │    (Spring Boot 3 / JRE)  │
        │    - JWT Filter           │
        │    - Business Logic       │
        └─────────────┬─────────────┘
                      │ tcp://db:3306 (Red interna)
                      ▼
        ┌───────────────────────────┐
        │        db (MySQL 8)       │
        │   - init.sql inicial      │
        │   - Volumen passtrack_db  │
        └───────────────────────────┘
```

---

## 6. Pipeline CI/CD con Jenkins y GitHub

La automatización de integración y despliegue continuo se gestiona mediante un flujo controlado por **Pull Requests** hacia dos ramas dedicadas de pipeline: **`test`** y **`prod`**. La rama `main` conserva únicamente la estructura inicial base del proyecto y no dispara pipelines de Jenkins.

### Flujo de Promoción y Disparo de Pipelines

```text
  main (Estructura inicial base del proyecto — sin despliegues)

  feat/<funcionalidad> ──(merge)──▶ develop ──(PR)──▶ test ──(PR)──▶ prod
                                                       │              │
                                                       ▼              ▼
                                               [Jenkins: Test] [Jenkins: Prod]
                                               Tests + Build   Dockerize + Deploy
                                               y Validación    Producción
```

### Comportamiento por Rama de Pipeline

1. **Pipeline de Pruebas (`develop` → PR hacia `test`):**
   - **Disparador:** Apertura o integración de un Pull Request desde `develop` hacia `test` notificado mediante Webhook de GitHub.
   - **Etapas ejecutadas por Jenkins:**
     1. **Checkout:** Clona el código promovido en la rama `test`.
     2. **Quality & Security Test:** Ejecuta `./mvnw test` en `PassTrack_backend` (pruebas unitarias y verificación de mínimo privilegio `403 Forbidden` con `MockMvc`) y el linter (`npm run lint`) en `PassTrack_frontend`.
     3. **Build Verification:** Compila el artefacto `.jar` de Spring Boot y el bundle estático de React (`npm run build`).
     4. **Staging Verification:** Construye y valida el arranque de los contenedores de prueba en Docker.
   - **Política estricta:** Si una prueba unitaria, de seguridad RBAC o de compilación falla, el pipeline se aborta inmediatamente e impide la promoción hacia `prod`.

2. **Pipeline de Producción (`test` → PR hacia `prod`):**
   - **Disparador:** Pull Request aprobado e integrado desde `test` hacia `prod` (únicamente después de que el pipeline de `test` haya finalizado en verde).
   - **Etapas ejecutadas por Jenkins:**
     1. **Checkout:** Clona la versión validada desde la rama `prod`.
     2. **Security & Build Gate:** Confirma la integridad del código y las pruebas críticas de seguridad JWT.
     3. **Dockerize:** Construye las imágenes inmutables multi-stage de producción para `PassTrack_backend` y `PassTrack_frontend`.
     4. **Continuous Deployment:** Ejecuta `docker compose up -d --remove-orphans`, aplicando la actualización de los servicios en producción sin intervención manual.

---

## 7. Guía de Puesta en Marcha (Instalación Rápida)

### Prerrequisitos
- Git 2.40+
- Docker Engine 24.0+ y Docker Compose v2+
- Java Development Kit (JDK) 21 (solo para desarrollo local sin Docker)
- Node.js 20 LTS y npm (solo para desarrollo local frontend)

### Paso 1: Clonar el repositorio
```bash
git clone git@github.com:<TU_USUARIO>/PassTrack.git
cd PassTrack
```

### Paso 2: Configurar las variables de entorno
Copia la plantilla de variables de entorno y ajusta los valores necesarios:
```bash
cp .env.example .env
```

Contenido representativo de `.env`:
```env
# Configuración MySQL
MYSQL_DATABASE=passtrack_db
MYSQL_USER=passtrack_user
MYSQL_PASSWORD=PassTrackSecret2026!
MYSQL_ROOT_PASSWORD=RootSuperSecure2026!

# Configuración Spring Boot
SPRING_PROFILES_ACTIVE=prod
DB_HOST=db
DB_PORT=3306
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=28800000

# Puertos
FRONTEND_PORT=80
BACKEND_PORT=8080
```

### Paso 3: Despliegue con Docker Compose (Recomendado)
Levanta todos los servicios orquestados en segundo plano:
```bash
docker compose up -d --build
```

Verifica el estado de los contenedores:
```bash
docker compose ps
```

- **Frontend:** Accede desde tu navegador a `http://localhost`
- **API Backend:** Accesible internamente a través de `http://localhost/api` o en `http://localhost:8080/api`

### Paso 4: Panel de Control Interactivo DX CLI (Node.js)

Para una experiencia de desarrollo interactiva y granular con multiplexación de logs coloreados y selección de servicios mediante teclado:

```bash
# Iniciar el panel interactivo
node dev-cli/dev.mjs

# O alternativamente desde la carpeta dev-cli/
cd dev-cli && npm start
```

Características del panel interactivo:
- **Menú con Checkbox:** Permite seleccionar interactivamente si levantar la Base de Datos (`docker compose up -d`), el Backend en Spring Boot (obligatorio) y/o el Frontend en React.
- **Logs Multiplexados:** Cada línea emitida por Java o Node incluye su respectivo prefijo de color (`[backend]`, `[frontend]`, `[db]`).
- **Apagado Limpio y Multiplataforma:** Captura `SIGINT` (<kbd>Ctrl</kbd> + <kbd>C</kbd>) y utiliza `taskkill` en Windows para evitar procesos huérfanos y liberar los puertos `8080` y `5173`.

### Paso 5: Inicialización Rápida Directa (Scripts de Arranque)

Si prefieres levantar todo directamente sin pasar por el menú interactivo:

**En Windows (PowerShell o CMD):**
```powershell
.\start.bat
```

**En Linux / macOS / Git Bash:**
```bash
chmod +x start.sh
./start.sh
```

El script ejecuta de manera orquestada en segundo plano:
- **Frontend Web (React 18 + Vite):** Disponible en [http://localhost:5173](http://localhost:5173) con Hot Module Replacement (HMR).
- **Backend REST API (Spring Boot 3 + Java 21):** Disponible en [http://localhost:8080](http://localhost:8080) con conexión JPA y seguridad JWT.
- **Endpoint Base:** [http://localhost:8080/api](http://localhost:8080/api)

> [!TIP]
> **Detención Limpia y Cierre de Puertos (Graceful Shutdown):** Al presionar `Ctrl + C`, el script intercepta la señal del sistema mediante `trap`, matando limpiamente los subprocesos de Java y Node y asegurando la liberación inmediata de los puertos `8080` y `5173`.

---

## 8. Datos Semilla para Pruebas (Seed Data & Credenciales Demo)

Para agilizar la evaluación de la plataforma en todos sus módulos y roles, el repositorio incluye un conjunto enriquecido de datos semilla de prueba (usuarios de todos los roles, grupos, inscripciones, asistencias históricas y justificantes con diversos dictámenes).

### 8.1. Comando para Sembrar los Datos

Puedes sembrar o reiniciar los datos de prueba en cualquier momento ejecutando el siguiente comando desde la raíz del proyecto:

```bash
# Mediante el CLI de Node.js (recomendado multiplataforma)
npm run seed
# o directamente:
node dev-cli/seed.mjs
```

**Alternativas de ejecución:**
- **Linux / macOS:**
  ```bash
  ./scripts/seed.sh
  ```
- **Directamente vía Docker Compose y SQL:**
  - **Windows (PowerShell):**
    ```powershell
    Get-Content PassTrack_backend/src/main/resources/seed.sql | docker compose exec -T db mysql -u passtrack_user -ppasstrack_user_secret passtrack_db
    ```
  - **Linux / macOS / Git Bash:**
    ```bash
    docker compose exec -T db mysql -u passtrack_user -ppasstrack_user_secret passtrack_db < PassTrack_backend/src/main/resources/seed.sql
    ```
- **Auto-sembrado:** Al arrancar el entorno con `node dev-cli/dev.mjs`, si la base de datos está vacía (tanto en MySQL en Docker como en H2 en memoria), `DataInitializer.java` sembrará automáticamente todos estos datos.

---

### 8.2. Catálogo de Cuentas Demo

| Nombre Completo | Correo Electrónico | Contraseña | Rol Asignado | Alcance y Acciones a Evaluar |
| :--- | :--- | :--- | :--- | :--- |
| **Dr. Roberto Gómez** | `director@passtrack.edu` | `Director123!` | `DIRECTOR` | **Control Total:** Gestión de usuarios y grupos, revisión de estadísticas globales y dictamen de justificantes pendientes. |
| **Mtra. Laura Morales** | `docente@passtrack.edu` | `Docente123!` | `DOCENTE` | **Docente Titular:** Pase de lista en `MAT-101` y `PROG-201`. Consulta de historial y justificaciones de sus alumnos. Intentar modificar `BD-301` retorna 403. |
| **Ing. Alejandro Martínez** | `prof.martinez@passtrack.edu` | `Docente123!` | `DOCENTE` | **Docente Titular:** Pase de lista y control en `BD-301`. |
| **Carlos Mendoza** | `alumno@passtrack.edu` | `Alumno123!` | `ALUMNO` | **Alumno:** Inscrito en `MAT-101` y `PROG-201`. Cuenta con 1 falta y 1 justificante `PENDIENTE`. |
| **María García** | `maria.garcia@passtrack.edu` | `Alumno123!` | `ALUMNO` | **Alumno:** Inscrita en `MAT-101` y `BD-301`. 100% de asistencia. |
| **Juan Pérez** | `juan.perez@passtrack.edu` | `Alumno123!` | `ALUMNO` | **Alumno:** Inscrito en `MAT-101` y `PROG-201`. Cuenta con 1 justificante `APROBADO` y 1 `RECHAZADO`. |
| **Sofía Hernández** | `sofia.hernandez@passtrack.edu` | `Alumno123!` | `ALUMNO` | **Alumno:** Inscrita en `MAT-101` y `BD-301`. Registros con retardos. |
| **Diego Torres** | `diego.torres@passtrack.edu` | `Alumno123!` | `ALUMNO` | **Alumno:** Inscrito en `MAT-101` y `BD-301`. Cuenta con faltas recientes. |
| **Valentina López** | `valentina.lopez@passtrack.edu` | `Alumno123!` | `ALUMNO` | **Alumno:** Inscrita en `MAT-101`. Cuenta con 1 justificante `PENDIENTE`. |
| **Mateo Ramírez** | `mateo.ramirez@passtrack.edu` | `Alumno123!` | `ALUMNO` | **Alumno:** Inscrito en `PROG-201` y `BD-301`. |
| **Camila Sánchez** | `camila.sanchez@passtrack.edu` | `Alumno123!` | `ALUMNO` | **Alumno:** Inscrita en `PROG-201` y `BD-301`. |

---

### 8.3. Entidades Sembradas para Pruebas Operativas

- **Grupos Escolares:**
  - `MAT-101`: *Matemáticas Avanzadas* (Titular: Mtra. Laura Morales) — 6 Alumnos inscritos.
  - `PROG-201`: *Estructuras de Datos y Algoritmos* (Titular: Mtra. Laura Morales) — 4 Alumnos inscritos.
  - `BD-301`: *Bases de Datos Relacionales* (Titular: Ing. Alejandro Martínez) — 5 Alumnos inscritos.
- **Histórico de Asistencias (43 registros):** Pases de lista realizados entre el `2026-11-03` y el `2026-11-07` que combinan asistencias (`PRESENTE`), faltas (`FALTA`), retardos (`RETARDO`) y asistencias justificadas (`JUSTIFICADO`).
- **Casos de Justificantes Precargados:**
  1. **Pendiente (Carlos Mendoza en `MAT-101`):** Cita médica familiar con comprobante en PDF listo para dictamen por el Director.
  2. **Aprobado (Juan Pérez en `MAT-101`):** Trámite de titulación y constancia de servicio social con dictamen favorable registrado.
  3. **Rechazado (Juan Pérez en `MAT-101`):** Inconveniente de transporte particular con dictamen de rechazo argumentado.
  4. **Pendiente (Valentina López en `MAT-101`):** Asistencia a congreso tecnológico estatal en espera de dictaminación.

---

## 9. Verificación Local y Control de Calidad

Antes de realizar cualquier commit o enviar cambios a GitHub, ejecuta el script de validación integral:

```bash
chmod +x scripts/check.sh
./scripts/check.sh
```

El script ejecuta de manera secuencial:
1. Pruebas de integración de seguridad y controladores en `PassTrack_backend` mediante Maven.
2. Validación de linter y compilación de producción en `PassTrack_frontend`.

---

## 10. Flujo de Git y Convenciones de Commits

Para mantener un historial limpio, auditable y alineado con los pipelines de Jenkins, el repositorio sigue una estructura estricta de ramas por propósito:

| Rama | Propósito | Regla de Integración |
| :--- | :--- | :--- |
| **`main`** | Conserva únicamente la estructura inicial del proyecto. | No recibe desarrollo ni dispara pipelines. |
| **`develop`** | Centraliza todo el desarrollo activo del proyecto. | Recibe merges provenientes de ramas de una sola funcionalidad. |
| **`feat/<tema>` / `fix/<tema>`** | Ramas dedicadas a una única funcionalidad o corrección. | Nacen desde `develop` y se integran de vuelta mediante merge a `develop`. |
| **`test`** | Rama de integración y pruebas en Jenkins. | Solo recibe cambios vía **PR desde `develop`**; dispara el pipeline de pruebas. |
| **`prod`** | Rama de despliegue productivo en Jenkins. | Solo recibe cambios vía **PR desde `test`**; dispara el pipeline de producción. |

### Formato de Commits (Conventional Commits)
Escribir los mensajes siguiendo la especificación de **Conventional Commits**:
- `feat: implementar filtro de JWT para validación de roles`
- `fix: corregir excepción al consultar asistencias de fecha duplicada`
- `test: agregar prueba de MockMvc para verificar 403 en rol Alumno`
- `docs: actualizar matriz de mínimo privilegio en el PRD`
- `ci: configurar stages diferenciados para test y prod en Jenkinsfile`