package com.passtrack.api.dto.usuario;

import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;

import java.time.Instant;

public record UsuarioResponseRecord(
    Long id,
    String nombre,
    String email,
    Rol rol,
    Boolean activo,
    Instant createdAt
) {
    public static UsuarioResponseRecord fromEntity(Usuario usuario) {
        return new UsuarioResponseRecord(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getRol(),
            usuario.getActivo(),
            usuario.getCreatedAt()
        );
    }
}
