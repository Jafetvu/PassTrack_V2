package com.passtrack.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestRecord(
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {
}
