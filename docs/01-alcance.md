# SOW · Alcance del Proyecto (PassTrack)

## 1. Objetivo
Desarrollar una plataforma web para el control de asistencias escolares y gestión de justificantes que aplique el principio de mínimo privilegio mediante JWT (`DIRECTOR`, `DOCENTE`, `ALUMNO`) y se actualice automáticamente en cada `push` a GitHub mediante un pipeline CI/CD con Jenkins y Docker.

## 2. Tabla de Decisiones

| # | Decisión | Alternativa descartada | Por qué | Fecha |
| :--- | :--- | :--- | :--- | :--- |
| **D-01** | Metodología Spec-Driven Development (SDD) combinada con Trunk-Based Development individual. | Programar sin especificación previa sobre una sola rama sin historial modular. | Permite que `docs/` defina los contratos de API, roles y reglas de negocio antes de codificar y probar en Jenkins. | 2026-11-05 |
| **D-02** | Frontend SPA en `PassTrack_frontend` con React + Vite servido en producción mediante Nginx en Docker. | Vistas monolíticas renderizadas en servidor (Thymeleaf/JSP). | Separa la capa de presentación del backend y permite construir contenedores independientes e inmutables. | 2026-11-05 |
| **D-03** | Backend REST en `PassTrack_backend` con Spring Boot 3, Spring Security y sesiones stateless con JWT. | Sesiones en memoria del servidor (`HttpSession`). | El token JWT porta el rol y el identificador del usuario, facilitando la validación sin estado entre contenedores y pruebas con `MockMvc`. | 2026-11-05 |
| **D-04** | Base de datos relacional MySQL 8.0 en contenedor con volumen persistente. | Base de datos NoSQL orientada a documentos. | El registro de asistencias exige integridad referencial y una llave única compuesta por `(alumno_id, grupo_id, fecha)`. | 2026-11-05 |
| **D-05** | El rol `DOCENTE` solo puede registrar asistencia en la fecha actual (`LocalDate.now()`) y en sus propios grupos. | Permitir a cualquier docente modificar asistencias de días pasados o de otros grupos. | Garantiza el principio de mínimo privilegio y obliga a que cualquier cambio posterior pase por el flujo de un justificante aprobado por el `DIRECTOR`. | 2026-11-05 |
| **D-06** | Almacenamiento de evidencias de justificantes (PDF/JPG) en un volumen Docker montado / servicio externo vía URL, guardando solo la ruta en MySQL. | Almacenar archivos binarios (`BLOB`) dentro de las tablas de MySQL. | Evita degradar el rendimiento de las consultas SQL y mantiene ligeros los respaldos del contenedor de base de datos. | 2026-11-05 |
| **D-07** | Pipeline CI/CD en Jenkins disparado por Webhook de GitHub que ejecuta tests y despliega con Docker Compose. | Despliegue manual compilando en local y subiendo artefactos por SSH. | Cumple el objetivo de automatización DevOps: ningún cambio actualiza los contenedores si antes no pasa la fase de pruebas automatizadas. | 2026-11-05 |

## 3. Módulos y Fases

* **Fase 1 · Cimiento de Seguridad y Gestión Base (Módulo Auth, Usuarios y Grupos):**
  * Autenticación con `/api/auth/login` emitiendo JWT firmado.
  * El `DIRECTOR` gestiona el alta de docentes, alumnos, grupos/materias e inscripciones.
* **Fase 2 · Operación de Pase de Lista (Módulo Asistencias):**
  * El `DOCENTE` visualiza exclusivamente sus grupos asignados y registra la lista del día (`PRESENTE`, `FALTA`, `RETARDO`).
  * El `ALUMNO` consulta su historial y porcentaje de asistencia por materia.
* **Fase 3 · Flujo de Justificantes y Automatización DevOps (Módulo Justificantes y Pipeline):**
  * El `ALUMNO` genera una solicitud de justificante vinculada a una falta propia adjuntando evidencia.
  * El `DIRECTOR` aprueba o rechaza la solicitud; si la aprueba, la asistencia cambia automáticamente a `JUSTIFICADO`.
  * Configuración final del `Jenkinsfile` y `docker-compose.yml` con actualización automática vía Webhook.

## 4. Plan Temporal

| Fase / Fecha límite | Hito | Entregable verificable | Responsable |
| :--- | :--- | :--- | :--- |
| **Hito 1 (08 Nov 2026)** | Especificación SDD y estructura base | `docs/` completos, `AGENTS.md` y estructura `PassTrack_backend` / `PassTrack_frontend`. | Jafet Bahena González |
| **Hito 2 (14 Nov 2026)** | Fase 1: Auth JWT, Roles y Grupos | Login JWT funcional, CRUD de usuarios/grupos y tests `403 Forbidden` en verde. | Jafet Bahena González |
| **Hito 3 (21 Nov 2026)** | Fase 2: Pase de Lista y Consulta | Endpoints de asistencia diaria y pantallas en React por rol. | Jafet Bahena González |
| **Hito 4 (28 Nov 2026)** | Fase 3: Justificantes y Pipeline CI/CD | Flujo de aprobación de justificantes y despliegue continuo con Jenkins + Docker. | Jafet Bahena González |

## 5. Riesgos

| # | Riesgo | Mitigación preventiva | Responsable |
| :--- | :--- | :--- | :--- |
| **R-01** | Un `push` a `main` introduce una regresión que rompe la seguridad de roles o la compilación. | El stage `Test` del `Jenkinsfile` ejecuta `./mvnw test` antes de construir imágenes; si falla una sola prueba, el pipeline se aborta y el contenedor actual sigue intacto. | Jafet Bahena González |
| **R-02** | El contenedor de `PassTrack_backend` intenta iniciar antes de que MySQL termine de levantar. | Configurar `healthcheck` con `mysqladmin ping` en `docker-compose.yml` y `depends_on: condition: service_healthy`. | Jafet Bahena González |
| **R-03** | Exposición de `JWT_SECRET` o credenciales de MySQL en el repositorio público/privado. | Ignorar `.env` en `.gitignore` e inyectar secretos mediante variables de entorno desde las credenciales seguras de Jenkins. | Jafet Bahena González |

## 6. Criterios de Aceptación de Alto Nivel
1. Cualquier petición a un endpoint protegido usando un JWT de un rol inferior o sin pertenencia sobre el recurso devuelve `403 Forbidden` en el **100 % de los casos**.
2. Un docente registra la asistencia completa de un grupo en **máximo 3 clics** y menos de **500 ms** de tiempo de respuesta de la API.
3. Al aprobar un justificante (`PATCH /api/justificantes/{id}/estado`), el registro de asistencia asociado actualiza su estado a `JUSTIFICADO` dentro de la misma transacción SQL.
4. Al realizar un `git push origin main`, Jenkins detecta el Webhook, ejecuta las pruebas, reconstruye las imágenes y actualiza los contenedores de `PassTrack_backend` y `PassTrack_frontend` en **menos de 4 minutos**.

## 7. Fuera de Alcance
* Edición libre de asistencias de fechas anteriores por parte del docente (`D-05`).
* Notificaciones por correo o SMS al aprobar justificantes.
* Aplicación móvil nativa (se entrega como SPA responsiva en `PassTrack_frontend`, `D-02`).

## 8. Documentos del Proyecto

| Documento | Estado | Razón dentro del flujo Spec-Driven Development |
| :--- | :--- | :--- |
| `docs/01-alcance.md` | **Incluido** | Define límites, fases, decisiones clave y criterios globales. |
| `docs/02-prd.md` | **Incluido** | Especifica historias de usuario, contratos de comportamiento (Dado/Cuando/Entonces) y NFRs. |
| `docs/03-integraciones.md` | **Incluido** | Documenta la conexión con MySQL, almacenamiento de archivos y GitHub Webhooks + Jenkins. |
| `docs/04-arquitectura.md` | **Incluido** | Define la topología de contenedores Docker y la matriz de aislamiento de mínimo privilegio. |
| `docs/05-datos.md` | **Incluido** | Establece el modelo relacional, restricciones de unicidad y reglas de borrado lógico. |
| `docs/06-convenciones.md` | **Incluido** | Estandariza los paquetes de código, DTOs, estrategia de testing y el pipeline CI/CD. |