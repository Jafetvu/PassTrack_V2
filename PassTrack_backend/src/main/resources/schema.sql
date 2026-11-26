-- ============================================================================
-- PassTrack · Esquema Inicial de Base de Datos (MySQL 8.0 / UTC)
-- Decisiones: M-01 (UNIQUE asistencia), M-02 (DATE / DATETIME UTC), M-03 (activo)
-- ============================================================================

CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    INDEX idx_usuario_rol_activo (rol, activo)
);

CREATE TABLE IF NOT EXISTS grupo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    clave VARCHAR(30) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    docente_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_grupo PRIMARY KEY (id),
    CONSTRAINT uk_grupo_clave UNIQUE (clave),
    CONSTRAINT fk_grupo_docente FOREIGN KEY (docente_id) REFERENCES usuario (id),
    INDEX idx_grupo_docente_activo (docente_id, activo)
);

CREATE TABLE IF NOT EXISTS inscripcion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    alumno_id BIGINT NOT NULL,
    grupo_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_inscripcion PRIMARY KEY (id),
    CONSTRAINT uk_inscripcion_alumno_grupo UNIQUE (alumno_id, grupo_id),
    CONSTRAINT fk_inscripcion_alumno FOREIGN KEY (alumno_id) REFERENCES usuario (id),
    CONSTRAINT fk_inscripcion_grupo FOREIGN KEY (grupo_id) REFERENCES grupo (id),
    INDEX idx_inscripcion_grupo (grupo_id)
);

CREATE TABLE IF NOT EXISTS asistencia (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    alumno_id BIGINT NOT NULL,
    grupo_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_asistencia PRIMARY KEY (id),
    CONSTRAINT uk_asistencia_alumno_grupo_fecha UNIQUE (alumno_id, grupo_id, fecha),
    CONSTRAINT fk_asistencia_alumno FOREIGN KEY (alumno_id) REFERENCES usuario (id),
    CONSTRAINT fk_asistencia_grupo FOREIGN KEY (grupo_id) REFERENCES grupo (id),
    INDEX idx_asistencia_grupo_fecha (grupo_id, fecha),
    INDEX idx_asistencia_alumno (alumno_id)
);

CREATE TABLE IF NOT EXISTS justificante (
    id BIGINT NOT NULL AUTO_INCREMENT,
    motivo VARCHAR(500) NOT NULL,
    evidencia_url VARCHAR(500) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    comentario_resolucion VARCHAR(500) NULL,
    asistencia_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    resolved_at DATETIME(6) NULL,
    CONSTRAINT pk_justificante PRIMARY KEY (id),
    CONSTRAINT uk_justificante_asistencia UNIQUE (asistencia_id),
    CONSTRAINT fk_justificante_asistencia FOREIGN KEY (asistencia_id) REFERENCES asistencia (id),
    INDEX idx_justificante_estado (estado)
);
