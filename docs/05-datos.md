# MODELO DE DATOS (PassTrack)

## 1. Decisiones de Datos

| # | Decisión | Alternativa descartada | Por qué | Fecha |
| :--- | :--- | :--- | :--- | :--- |
| **M-01** | `UNIQUE (alumno_id, grupo_id, fecha)` en Asistencia. | Validar en Java con un SELECT. | Bloqueo absoluto de pases de lista duplicados por concurrencia. | 2026-10-22 |
| **M-02** | Fechas como `DATE`, auditoría como `DATETIME` UTC. | Formato `VARCHAR`. | Permite validar nativamente `fecha == LocalDate.now()` en SQL/Java. | 2026-10-22 |
| **M-03** | Borrado lógico (`activo = false`) en usuarios. | Borrado físico `DELETE`. | Previene la eliminación en cascada de historiales académicos. | 2026-10-22 |

## 2. ERD Conceptual (Mermaid)

```mermaid
erDiagram
    USUARIO ||--o{ GRUPO : imparte
    USUARIO ||--o{ INSCRIPCION : cursa
    GRUPO ||--o{ INSCRIPCION : contiene
    USUARIO ||--o{ ASISTENCIA : recibe
    GRUPO ||--o{ ASISTENCIA : registra
    ASISTENCIA ||--o| JUSTIFICANTE : origina

    USUARIO {
        Long id PK
        String email UK
        String password_hash
        String rol "DIRECTOR,DOCENTE,ALUMNO"
        Boolean activo
    }
    GRUPO {
        Long id PK
        String clave UK
        Long docente_id FK
    }
    INSCRIPCION {
        Long id PK
        Long alumno_id FK
        Long grupo_id FK
    }
    ASISTENCIA {
        Long id PK
        LocalDate fecha
        String estado "PRESENTE,FALTA,RETARDO,JUSTIFICADO"
        Long alumno_id FK
        Long grupo_id FK
    }
    JUSTIFICANTE {
        Long id PK
        String estado "PENDIENTE,APROBADO,RECHAZADO"
        Long asistencia_id FK_UK
    }
```

## 3. Reglas Transversales
* Contraseñas jamás retornadas en JSON (ignoradas con `@JsonIgnore` o DTOs aislados).
* Un justificante por asistencia (`asistencia_id` es Unique y Foreign Key).