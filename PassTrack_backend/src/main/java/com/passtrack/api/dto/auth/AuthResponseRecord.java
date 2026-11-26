package com.passtrack.api.dto.auth;

import com.passtrack.api.model.Rol;

public record AuthResponseRecord(
    String token,
    Long userId,
    String nombre,
    String email,
    Rol rol
) {
}
