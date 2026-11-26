package com.passtrack.api.dto.grupo;

import com.passtrack.api.model.Grupo;

public record GrupoResponseRecord(
    Long id,
    String clave,
    String nombre,
    Long docenteId,
    String docenteNombre,
    Boolean activo,
    long totalAlumnos
) {
    public static GrupoResponseRecord fromEntity(Grupo grupo, long totalAlumnos) {
        return new GrupoResponseRecord(
            grupo.getId(),
            grupo.getClave(),
            grupo.getNombre(),
            grupo.getDocente().getId(),
            grupo.getDocente().getNombre(),
            grupo.getActivo(),
            totalAlumnos
        );
    }
}
