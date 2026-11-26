# BACKLOG · Product Backlog del Sistema PassTrack

Bienvenido al Product Backlog oficial de **PassTrack**. Este documento define la secuencia cronológica de implementación bajo la metodología **Spec-Driven Development (SDD)**, comenzando desde el "commit cero" hasta la entrega continua en producción. 

Las tareas están organizadas con casillas interactivas (`- [ ]`) listas para seguimiento o conversión a *Issues* de GitHub.

---

## Épica 1: Cimientos y DevOps Inicial

- [x] **PASS-01: Configurar el script sensor unificado scripts/check.sh para validaciones locales y CI/CD**
  - *Descripción:* Crear el script ejecutable `scripts/check.sh` que actúa como sensor bloqueante del monorepo, ejecutando secuencialmente `./mvnw test -B` en `PassTrack_backend` y `npm run lint && npm run build` en `PassTrack_frontend`.
  - *Criterio de Aceptación:* El script corre con `set -e`, aborta el flujo ante cualquier fallo en backend o frontend, y finaliza con código de salida `0` cuando ambas suites de validación pasan en verde.

- [x] **PASS-02: Definir el entorno multi-contenedor con Docker Compose (docker-compose.yml)**
  - *Descripción:* Configurar la orquestación en `docker-compose.yml` para los 3 contenedores aislados: `db` (`mysql:8.0`), `PassTrack_backend` (Spring Boot 3 en JRE 21) y `PassTrack_frontend` (Nginx + React SPA), unidos por la red bridge privada `passtrack_net` y parametrizados mediante `.env.example`.
  - *Criterio de Aceptación:* `docker compose up -d` inicializa los servicios; el contenedor `db` cuenta con `healthcheck` funcional (`mysqladmin ping`) y el puerto 3306 no queda expuesto públicamente al host.

- [x] **PASS-03: Crear Dockerfile multi-stage optimizado para PassTrack_backend**
  - *Descripción:* Configurar `PassTrack_backend/Dockerfile` en dos etapas: etapa 1 de compilación con Maven 3.9 y Eclipse Temurin JDK 21, y etapa 2 ligera de ejecución con Eclipse Temurin JRE 21 Alpine, ejecutando la aplicación con un usuario sin privilegios root sobre el puerto interno 8080.
  - *Criterio de Aceptación:* La imagen generada pesa menos de 300 MB, excluye código fuente o utilidades de compilación, y arranca la API Spring Boot de forma limpia mediante `java -jar app.jar`.

- [x] **PASS-04: Crear Dockerfile multi-stage y proxy inverso Nginx para PassTrack_frontend**
  - *Descripción:* Configurar `PassTrack_frontend/Dockerfile` (Node 20 Alpine para el build de Vite y Nginx 1.25 Alpine para el servidor web) junto con `PassTrack_frontend/nginx.conf` configurado como proxy inverso para redirigir `/api/*` hacia `http://PassTrack_backend:8080` y servir el SPA con fallback a `index.html`.
  - *Criterio de Aceptación:* El contenedor responde en el puerto 80, resuelve rutas SPA sin error 404 ante recargas de página y redirige el tráfico de `/api/*` al backend eliminando problemas de CORS en producción.

- [x] **PASS-05: Diseñar el pipeline declarativo CI/CD en Jenkinsfile con trigger por Webhook**
  - *Descripción:* Crear el archivo `Jenkinsfile` con etapas de `Checkout`, `Sensor Validation` (ejecutando `./scripts/check.sh`), `Build Docker Images` y `Deploy Containers`, integrando inyección de credenciales seguras (`DB_PASSWORD`, `JWT_SECRET`) y disparo automático por GitHub Webhook en `main`.
  - *Criterio de Aceptación:* Un `git push` a `main` dispara automáticamente el pipeline; si `check.sh` falla, el build se marca como `FAILURE` sin sustituir los contenedores en ejecución; el ciclo total completa en menos de 4 minutos.

---

## Épica 2: Base de Datos y Core Backend

- [x] **PASS-06: Crear script de migración inicial init.sql con restricciones de integridad**
  - *Descripción:* Diseñar el script SQL en `PassTrack_backend/src/main/resources/schema.sql` (y montaje para `db` en `/docker-entrypoint-initdb.d/init.sql`) con las tablas `usuario`, `grupo`, `inscripcion`, `asistencia` y `justificante`, definiendo índices, llaves foráneas y la restricción `UNIQUE (alumno_id, grupo_id, fecha)`.
  - *Criterio de Aceptación:* El script se ejecuta limpiamente en MySQL 8.0, impide duplicados de asistencias concurrentes a nivel de motor relacional (`M-01`) y maneja marcas de tiempo en formato UTC (`M-02`).

- [x] **PASS-07: Implementar entidades JPA en la capa Modelo (model/)**
  - *Descripción:* Crear las clases `@Entity` en el paquete `com.passtrack.api.model` (`Usuario`, `Grupo`, `Inscripcion`, `Asistencia`, `Justificante`), definiendo relaciones `@ManyToOne`, `@OneToMany`, `@OneToOne`, enumeraciones (`Rol`, `EstadoAsistencia`, `EstadoJustificante`) y el atributo booleano `activo` para borrado lógico (`M-03`).
  - *Criterio de Aceptación:* El mapeo ORM es validado mediante pruebas de persistencia; el campo `passwordHash` no se expone en serializaciones; la suite de `check.sh` compila sin advertencias de Hibernate.

- [x] **PASS-08: Implementar repositorios Spring Data JPA en la capa Repositorio (repository/)**
  - *Descripción:* Desarrollar las interfaces que extienden `JpaRepository` en `com.passtrack.api.repository` (`UsuarioRepository`, `GrupoRepository`, `InscripcionRepository`, `AsistenciaRepository`, `JustificanteRepository`), aplicando el Repository Pattern con métodos de consulta derivados (`findByEmail`, `findByDocenteIdAndActivoTrue`, `findByGrupoIdAndFecha`).
  - *Criterio de Aceptación:* Pruebas de integración con `@DataJpaTest` validando consultas personalizadas, filtrado de borrado lógico y búsqueda por clave compuesta única sin uso de SQL nativo vulnerable.

---

## Épica 3: Seguridad y Autenticación

- [x] **PASS-09: Configurar la cadena de seguridad y utilidades criptográficas JWT en Backend**
  - *Descripción:* Implementar `JwtService` (firma y validación HMAC-SHA256 con claims `sub`, `userId`, `rol` y expiración en 8 horas / `NFR-01`) y `JwtAuthenticationFilter` en `security/` extendiendo `OncePerRequestFilter` (Chain of Responsibility) para poblar el `SecurityContextHolder`. Configurar `SecurityFilterChain` en modo `STATELESS` habilitando `@EnableMethodSecurity`.
  - *Criterio de Aceptación:* Cualquier petición a `/api/**` sin token Bearer válido responde `401 Unauthorized` (excepto `/api/auth/login` y `/actuator/health`); tokens expirados o firmas manipuladas son rechazados de inmediato.

- [x] **PASS-10: Implementar endpoint de Login con DTOs inmutables (Java Records) en Controller y Service**
  - *Descripción:* Definir los DTOs inmutables `LoginRequestRecord` y `AuthResponseRecord` con Java Records en `dto/auth/`. En `AuthService`, validar credenciales con `AuthenticationManager` y `BCryptPasswordEncoder`. En `AuthController` (`@RestController`), exponer `POST /api/auth/login` respetando el flujo estricto Multicapa (Controller invoca Service).
  - *Criterio de Aceptación:* Petición con credenciales correctas retorna `200 OK` con JWT y rol del usuario sin exponer `passwordHash`; credenciales incorrectas devuelven `401 Unauthorized`; pruebas unitarias con `MockMvc` pasan en verde.

- [x] **PASS-11: Configurar cliente HTTP Axios con Interceptor Pattern en Frontend**
  - *Descripción:* Crear la instancia centralizada en `PassTrack_frontend/src/api/axiosClient.js`, implementando un interceptor de solicitud para adjuntar automáticamente el encabezado `Authorization: Bearer <token>` y un interceptor de respuesta para interceptar errores `401` y `403`, limpiando el estado de sesión y redirigiendo a la ruta `/login`.
  - *Criterio de Aceptación:* Toda petición HTTP originada en la aplicación incluye el token JWT activo; ante una respuesta `401`, la sesión local se purga y el navegador navega a `/login` sin generar excepciones no capturadas.

- [x] **PASS-12: Implementar estado global de autenticación con Provider Pattern (AuthContext) en Frontend**
  - *Descripción:* Crear `src/context/AuthContext.jsx` y su hook personalizado `useAuth`, proveyendo de manera reactiva el usuario autenticado, token, rol y métodos `login` / `logout`. Decodificar los claims del token en el montaje inicial para rehidratar la sesión de forma transparente.
  - *Criterio de Aceptación:* El estado de sesión se propaga a los componentes sin *prop drilling*; el estado persiste ante recargas del navegador; `logout` destruye el token en almacenamiento local y restablece el contexto a valores nulos.

- [x] **PASS-13: Desarrollar módulo de Login con componentes Smart/Dumb en src/features/auth/**
  - *Descripción:* Estructurar la feature `src/features/auth/` implementando el componente contenedor inteligente `LoginContainer.jsx` (gestión de estado de formulario, invocación del servicio de autenticación y redirección condicionada por rol) y el componente presentacional tonto `LoginForm.jsx` (renderizado puro de inputs, botones y alertas visuales recibidas por props).
  - *Criterio de Aceptación:* Autenticación exitosa redirige al dashboard correspondiente (`DIRECTOR`, `DOCENTE`, `ALUMNO`); credenciales incorrectas despliegan mensaje de error visual sin recargar la página; `npm run lint` pasa sin advertencias.

---

## Épica 4: Fase 1 - Gestión de Usuarios y Grupos

- [x] **PASS-14: Implementar DTOs inmutables (Java Records) y Servicio de Usuarios con cifrado BCrypt**
  - *Descripción:* Crear los DTOs `UsuarioCreateRecord`, `UsuarioResponseRecord` y `UsuarioUpdateRecord` en `dto/usuario/`. En `UsuarioService`, implementar la lógica de negocio para crear usuarios cifrando contraseñas con `BCryptPasswordEncoder`, listar usuarios activos con paginación y realizar borrado lógico cambiando el flag `activo` a `false`.
  - *Criterio de Aceptación:* Ningún DTO expone `passwordHash`; intentos de registro con email duplicado lanzan excepción de conflicto (`409 Conflict`); el borrado lógico no borra registros físicamente (`M-03`); pruebas unitarias en `UsuarioServiceTest` en verde.

- [x] **PASS-15: Desarrollar UsuarioController con autorización RBAC estricta (@PreAuthorize)**
  - *Descripción:* Implementar en `UsuarioController` los endpoints REST `POST /api/usuarios`, `GET /api/usuarios`, `GET /api/usuarios/{id}` y `DELETE /api/usuarios/{id}`, restringiendo cada operación exclusivamente al rol `DIRECTOR` mediante `@PreAuthorize("hasRole('DIRECTOR')")`. El controlador únicamente recibe y responde Java Records e invoca a `UsuarioService`.
  - *Criterio de Aceptación:* Peticiones autenticadas como `DIRECTOR` responden `201 Created` / `200 OK`; peticiones realizadas por `DOCENTE` o `ALUMNO` son bloqueadas con `403 Forbidden` (`UsuarioControllerTest#crearUsuario_SinRolDirector_Retorna403`); `check.sh` pasa en verde.

- [x] **PASS-16: Desarrollar DTOs, Servicio y Controlador para Grupos e Inscripciones**
  - *Descripción:* Crear los DTOs `GrupoCreateRecord`, `GrupoResponseRecord` e `InscripcionCreateRecord`. Desarrollar `GrupoService` (validando existencia del docente titular y unicidad de clave de grupo) y `GrupoController` protegido con `@PreAuthorize("hasRole('DIRECTOR')")`, manteniendo el flujo estricto Controller -> Service -> Repository.
  - *Criterio de Aceptación:* Impide asociar docentes inactivos o inexistentes (`400 Bad Request`); peticiones de usuarios sin rol `DIRECTOR` devuelven `403 Forbidden`; pruebas de integración con `MockMvc` pasan en verde.

- [x] **PASS-17: Desarrollar módulo de administración de usuarios en src/features/usuarios/**
  - *Descripción:* Construir en `src/features/usuarios/` el componente inteligente `UsuariosAdminContainer.jsx` (estado local, consumo de API vía Axios, filtrado por rol y modal de confirmación) y los componentes tontos `TablaUsuarios.jsx` y `ModalUsuarioForm.jsx` (encargados puramente de la presentación y emisión de eventos mediante props).
  - *Criterio de Aceptación:* El Director puede crear, filtrar y dar de baja lógica a docentes y alumnos; validación en formulario cliente antes del envío; rutas protegidas redirigen fuera del módulo a usuarios con roles no autorizados.

- [] **PASS-18: Desarrollar módulo de gestión de grupos e inscripciones en src/features/grupos/**
  - *Descripción:* Construir en `src/features/grupos/` el componente contenedor `GruposContainer.jsx` (gestión de asignación de docentes titulares e inscripción de alumnos en grupos) y los componentes presentacionales `ListaGruposCard.jsx` y `AsignarAlumnoModal.jsx` desacoplados de llamadas a la red.
  - *Criterio de Aceptación:* Permite crear grupos y matricular alumnos visualizando la lista actualizada en tiempo real; validación visual de formularios; `npm run lint` y `npm run build` pasan sin incidencias.

---

## Épica 5: Fase 2 - Operación de Pase de Lista

- [] **PASS-19: Implementar DTOs inmutables para pase de lista y resúmenes de asistencia**
  - *Descripción:* Crear los Java Records en `dto/asistencia/`: `PaseListaItemRecord` (`alumnoId`, `estado`), `RegistroAsistenciaBulkRecord` (`fecha`, `List<PaseListaItemRecord> lista`), `AsistenciaResponseRecord` y `ResumenAsistenciaAlumnoRecord` (porcentajes, total de faltas, retardos y justificados).
  - *Criterio de Aceptación:* Los Java Records garantizan inmutabilidad absoluta; validan restricciones `@NotNull` en sus campos; ninguna entidad `@Entity` de persistencia es expuesta en estos contratos.

- [] **PASS-20: Implementar AsistenciaService con validación de titularidad (Ownership) y fecha actual**
  - *Descripción:* Desarrollar en `AsistenciaService` la lógica de pase de lista bajo `@Transactional`: verificar que el docente autenticado sea el titular asignado al grupo en la base de datos (validación de Ownership), validar que `fecha.equals(LocalDate.now())` (`D-05`), y comprobar que no exista pase previo en la fecha actual (lanzando `409 Conflict` si ya existe).
  - *Criterio de Aceptación:* Retorna `403 Forbidden` si un docente intenta pasar lista en un grupo ajeno (`H-02`); retorna `400 Bad Request` si la fecha difiere del día en curso; retorna `409 Conflict` ante duplicidad; rollback atómico total ante fallo (`NFR-02`).

- [] **PASS-21: Implementar AsistenciaController para registro docente y consulta por alumno**
  - *Descripción:* Crear los endpoints REST en `AsistenciaController`: `POST /api/asistencias/grupo/{id}` con `@PreAuthorize("hasRole('DOCENTE')")`, `GET /api/asistencias/grupo/{id}/hoy`, y `GET /api/asistencias/mis-asistencias` con `@PreAuthorize("hasRole('ALUMNO')")` asegurando que el servicio filtre estrictamente por el `userId` autenticado.
  - *Criterio de Aceptación:* Pruebas unitarias en `AsistenciaControllerTest`: docente titular registra asistencia obteniendo `201 Created` en menos de 500 ms; docente no titular recibe `403 Forbidden`; alumno obtiene solo sus registros propios; `check.sh` pasa en verde.

- [] **PASS-22: Desarrollar módulo de Pase de Lista docente en src/features/asistencias/**
  - *Descripción:* Implementar en `src/features/asistencias/` el componente contenedor `PaseListaContainer.jsx` (obtiene la nómina del grupo, preselecciona a todos los alumnos en `PRESENTE` según `P-01`, gestiona estado de guardado) y el componente presentacional `TablaPaseLista.jsx` con botones de alternancia rápida a `FALTA` o `RETARDO`.
  - *Criterio de Aceptación:* El docente completa el pase de lista de su grupo en un máximo de 3 clics; los alumnos aparecen inicialmente en `PRESENTE`; deshabilita la edición si el pase del día ya fue registrado; notificaciones de éxito y error accesibles.

- [] **PASS-23: Desarrollar módulo de consulta de asistencias para Alumnos en src/features/asistencias/**
  - *Descripción:* Implementar en `src/features/asistencias/` el contenedor `MisAsistenciasContainer.jsx` (consumo de `GET /api/asistencias/mis-asistencias` con Axios) y los componentes presentacionales `ResumenPorcentajeCard.jsx` e `HistorialAsistenciasTable.jsx`, mostrando porcentaje global por materia y botón condicional "Solicitar Justificante" en registros con falta.
  - *Criterio de Aceptación:* El alumno visualiza su desglose con semáforo de colores; el botón "Solicitar Justificante" solo se visualiza activo en registros con estado `FALTA` o `RETARDO` que no cuenten con solicitud previa (`P-02`).

---

## Épica 6: Fase 3 - Gestión de Justificantes y Storage

- [] **PASS-24: Implementar servicio de almacenamiento de comprobantes en volumen Docker**
  - *Descripción:* Crear `FileStorageService` para almacenar evidencias en el volumen persistente `/app/uploads` (`I-01` / `D-06`). Validar que los archivos no superen `2 MB` y cumplan tipos MIME permitidos (`application/pdf`, `image/jpeg`, `image/png`). Retornar ruta segura con identificador UUID único.
  - *Criterio de Aceptación:* Archivos que excedan 2 MB o con formato inválido son rechazados con `400 Bad Request`; pruebas unitarias con mock de almacenamiento (`@MockBean`) aseguran ejecución exitosa y aislada en el entorno de Jenkins.

- [] **PASS-25: Implementar DTOs inmutables y Servicio de Justificantes con validación de propiedad**
  - *Descripción:* Crear los DTOs `JustificanteCreateRecord` (`asistenciaId`, `motivo`), `JustificanteResolucionRecord` (`estado`, `comentarioResolucion`) y `JustificanteResponseRecord`. En `JustificanteService`, verificar que la asistencia pertenezca al alumno autenticado (`Ownership`) y que no exista un justificante previo vinculado (`asistencia_id UNIQUE`).
  - *Criterio de Aceptación:* Intentar solicitar justificante para una falta ajena retorna `403 Forbidden` (`H-03`); registrar más de un justificante para la misma falta retorna `409 Conflict` (`P-02`); la solicitud se inicializa en estado `PENDIENTE`.

- [] **PASS-26: Implementar dictamen de justificantes con mutación atómica transaccional de asistencia**
  - *Descripción:* Implementar en `JustificanteService.resolverJustificante` la lógica bajo `@Transactional`: actualizar el justificante a `APROBADO` o `RECHAZADO` con el comentario del Director; ante aprobación, mutar automáticamente el estado de la entidad `Asistencia` vinculada a `JUSTIFICADO` dentro de la misma transacción relacional (`P-03`).
  - *Criterio de Aceptación:* Al aprobar el justificante, la asistencia vinculada actualiza su estado a `JUSTIFICADO` en MySQL; ante cualquier fallo imprevisto, se realiza un rollback integral de ambas entidades; prueba `JustificanteServiceTest#resolverJustificante_Aprobado_CambiaAsistenciaAJustificado` en verde.

- [] **PASS-27: Implementar JustificanteController para Alumnos y Director con control RBAC**
  - *Descripción:* Exponer endpoints REST en `JustificanteController`: `POST /api/justificantes` multipart protegido con `@PreAuthorize("hasRole('ALUMNO')")`, `GET /api/justificantes/pendientes` con `@PreAuthorize("hasRole('DIRECTOR')")`, `PATCH /api/justificantes/{id}/estado` con `@PreAuthorize("hasRole('DIRECTOR')")`, y descarga de evidencia autorizada.
  - *Criterio de Aceptación:* Docentes y alumnos reciben `403 Forbidden` al invocar `PATCH /api/justificantes/{id}/estado` (`H-04`); el Director aprueba justificantes y recibe `200 OK`; pruebas con `MockMvc` pasan en verde.

- [] **PASS-28: Desarrollar módulo de solicitud de justificante en src/features/justificantes/**
  - *Descripción:* Construir en `src/features/justificantes/` el componente inteligente `SolicitarJustificanteContainer.jsx` (gestión de `FormData`, validación en cliente de tamaño y formato de archivo, llamada a API) y el componente presentacional `SolicitudJustificanteModal.jsx` con formulario de motivo y selector de archivo.
  - *Criterio de Aceptación:* El formulario valida archivo y motivo antes de enviar; muestra indicador visual de carga durante la transferencia; al completarse con éxito, refresca la tabla de asistencias y cierra el modal sin errores.

- [] **PASS-29: Desarrollar módulo de dictamen de justificantes para el Director en src/features/justificantes/**
  - *Descripción:* Construir en `src/features/justificantes/` el componente inteligente `BandejaJustificantesContainer.jsx` (carga de solicitudes pendientes, ejecución de dictamen con comentario obligatorio) y los componentes tontos `TablaJustificantesPendientes.jsx` y `VisorEvidenciaModal.jsx` (previsualización de archivo y captura de resolución).
  - *Criterio de Aceptación:* El Director previsualiza la evidencia (PDF o imagen); el dictamen requiere comentario de resolución antes de habilitar el botón; al resolverse, el registro se remueve de la bandeja en tiempo real; `npm run lint` pasa en verde.

- [] **PASS-30: Validación integral end-to-end del pipeline CI/CD y despliegue automatizado**
  - *Descripción:* Ejecutar la verificación integral de extremo a extremo: correr `./scripts/check.sh`, realizar `push` a `main` en GitHub, comprobar la recepción del Webhook en Jenkins, la ejecución de pruebas unitarias y de mínimo privilegio (403), y el despliegue automático con Docker Compose.
  - *Criterio de Aceptación:* El pipeline completa todas sus etapas en menos de 4 minutos (`NFR-03`); el 100% de las pruebas de RBAC y propiedad de datos finalizan en verde; los tres contenedores (`db`, `PassTrack_backend`, `PassTrack_frontend`) se mantienen saludables en producción.