# PRD · Especificación de Producto (PassTrack)

## 1. Tabla de Decisiones de Producto

| # | Decisión | Alternativa descartada | Por qué | Fecha |
| :--- | :--- | :--- | :--- | :--- |
| **P-01** | En la vista de pase de lista, todos los alumnos inscritos aparecen preseleccionados como `PRESENTE`. | Obligar al docente a seleccionar manualmente el estado de cada alumno desde vacío. | Reduce la fricción operativa: el docente solo cambia a `FALTA` o `RETARDO` a quienes no asistieron. | 2026-11-05 |
| **P-02** | Un alumno solo puede enviar un justificante por cada registro con estado `FALTA` o `RETARDO`. | Permitir múltiples solicitudes de justificante para la misma falta. | Evita duplicidad en la bandeja del `DIRECTOR` mediante una relación `1:1` (`UNIQUE` en `asistencia_id`). | 2026-11-05 |
| **P-03** | La aprobación del justificante muta automáticamente la asistencia vinculada a `JUSTIFICADO`. | Requerir que el docente modifique la lista manualmente después de la autorización. | Mantiene la integridad de auditoría y respeta la regla `D-05` de no edición manual retroactiva. | 2026-11-05 |

## 2. Historias de Usuario y Especificación de Criterios

### H-01 · Administración de usuarios, grupos e inscripciones
* **Historia:** Como **Director**, quiero dar de alta usuarios (`DOCENTE`, `ALUMNO`), crear grupos y asignar alumnos para estructurar las listas de asistencia del periodo escolar.
* **Módulo del SOW:** Módulo Auth, Usuarios y Grupos · Fase 1 · **Prioridad:** Alta.
* **Criterios de aceptación:**
  1. **Dado** un usuario autenticado con rol `DIRECTOR`, **cuando** envía `POST /api/usuarios` con datos válidos, **entonces** el sistema cifra la contraseña con `BCrypt`, guarda el usuario y responde `201 Created` sin incluir `passwordHash` en el JSON (Prueba: `UsuarioControllerTest#crearUsuario_ComoDirector_Retorna201`).
  2. **Dado** un usuario con rol `DOCENTE` o `ALUMNO`, **cuando** intenta ejecutar `POST /api/usuarios` o `POST /api/grupos`, **entonces** Spring Security bloquea la petición con `403 Forbidden` (Prueba: `UsuarioControllerTest#crearUsuario_SinRolDirector_Retorna403`).

### H-02 · Pase de lista diario con validación de titularidad
* **Historia:** Como **Docente**, quiero registrar la asistencia del día actual en mis grupos asignados para llevar el control diario de asistencias.
* **Módulo del SOW:** Módulo Asistencias · Fase 2 · **Prioridad:** Alta.
* **Criterios de aceptación:**
  1. **Dado** un usuario con rol `DOCENTE` que es titular del grupo `ID=1`, **cuando** envía `POST /api/asistencias/grupo/1` con la lista de estados de hoy, **entonces** se guardan los registros y devuelve `201 Created` (Prueba: `AsistenciaControllerTest#registrarLista_DocenteTitular_Retorna201`).
  2. **Dado** un usuario con rol `DOCENTE` que NO es titular del grupo `ID=2`, **cuando** intenta enviar `POST /api/asistencias/grupo/2`, **entonces** el servicio rechaza la operación con `403 Forbidden` (Prueba: `AsistenciaControllerTest#registrarLista_DocenteNoTitular_Retorna403`).
  3. **Dado** un docente titular, **cuando** intenta registrar asistencia para un grupo donde ya se pasó lista en la fecha actual, **entonces** el sistema devuelve `409 Conflict` evitando registros duplicados (Prueba: `AsistenciaServiceTest#registrarLista_FechaDuplicada_LanzaExcepcion409`).

### H-03 · Consulta de asistencias y solicitud de justificante
* **Historia:** Como **Alumno**, quiero consultar mis asistencias y enviar una solicitud de justificante sobre una falta propia adjuntando el motivo y el comprobante digital.
* **Módulo del SOW:** Módulo Justificantes · Fase 3 · **Prioridad:** Alta.
* **Criterios de aceptación:**
  1. **Dado** un usuario con rol `ALUMNO` dueño de una asistencia con estado `FALTA`, **cuando** envía `POST /api/justificantes` indicando `asistenciaId`, `motivo` y archivo/URL de evidencia, **entonces** el justificante se registra en estado `PENDIENTE` y retorna `201 Created` (Prueba: `JustificanteControllerTest#solicitarJustificante_FaltaPropia_Retorna201`).
  2. **Dado** un usuario con rol `ALUMNO`, **cuando** intenta solicitar un justificante para la `asistenciaId` de otro compañero, **entonces** el backend responde `403 Forbidden` (Prueba: `JustificanteControllerTest#solicitarJustificante_FaltaAjena_Retorna403`).

### H-04 · Dictamen de justificantes (Aprobación / Rechazo)
* **Historia:** Como **Director**, quiero listar los justificantes pendientes y aprobarlos o rechazarlos con un comentario de resolución para actualizar el estatus académico del alumno.
* **Módulo del SOW:** Módulo Justificantes · Fase 3 · **Prioridad:** Alta.
* **Criterios de aceptación:**
  1. **Dado** un usuario con rol `DIRECTOR` y un justificante en estado `PENDIENTE`, **cuando** envía `PATCH /api/justificantes/{id}/estado` con `{"estado": "APROBADO", "comentarioResolucion": "Receta médica validada"}`, **entonces** el justificante pasa a `APROBADO`, la asistencia vinculada cambia a `JUSTIFICADO` y devuelve `200 OK` (Prueba: `JustificanteServiceTest#resolverJustificante_Aprobado_CambiaAsistenciaAJustificado`).
  2. **Dado** un usuario con rol `DOCENTE` o `ALUMNO`, **cuando** intenta invocar `PATCH /api/justificantes/{id}/estado`, **entonces** recibe `403 Forbidden` (Prueba: `JustificanteControllerTest#resolverJustificante_RolNoAutorizado_Retorna403`).

## 3. Requisitos No Funcionales (NFR)
1. **Seguridad JWT y Mínimo Privilegio (NFR-01):** Los tokens JWT se firman con algoritmo `HS256`, incluyen los claims `sub` (email), `userId` y `rol`, y expiran en **8 horas** (`28800000 ms`). Ninguna ruta `/api/**` (excepto `/api/auth/login` y `/actuator/health`) es pública.
2. **Integridad Transaccional (NFR-02):** Las operaciones compuestas (pase de lista grupal y aprobación de justificante con actualización de asistencia) se ejecutan bajo `@Transactional` en Spring Boot, haciendo *rollback* completo ante cualquier fallo.
3. **Portabilidad y Tiempo de Construcción en CI/CD (NFR-03):** La construcción multi-stage de Docker para `PassTrack_backend` y `PassTrack_frontend` debe completarse y levantar contenedores saludables en **menos de 4 minutos** dentro del agente de Jenkins.

## 4. Fuera de Alcance
* Modificación de asistencias por parte de docentes en fechas distintas al día en curso (`D-05`).
* Registro abierto de usuarios (sign-up público): solo el `DIRECTOR` puede crear cuentas nuevas para mantener el control de acceso institucional.