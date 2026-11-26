package com.passtrack.api.security;

import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final String OTHER_SECRET = "505E635266556A586E3272357538782F413F4428472B4B6250645367566B5971";

    @Test
    void generateToken_IncluyeClaimsSubUserIdYRolYValidaCorrectamente() {
        JwtService jwtService = new JwtService(SECRET, 28800000L);
        Usuario usuario = Usuario.builder()
            .id(42L)
            .nombre("Dr. Roberto Gómez")
            .email("director@passtrack.edu")
            .passwordHash("hash")
            .rol(Rol.DIRECTOR)
            .activo(true)
            .build();
        CustomUserDetails userDetails = new CustomUserDetails(usuario);

        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractEmail(token)).isEqualTo("director@passtrack.edu");
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtService.extractRol(token)).isEqualTo("DIRECTOR");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void tokenExpirado_LanzaExpiredJwtException() {
        JwtService expiredJwtService = new JwtService(SECRET, -1000L);
        String token = expiredJwtService.generateToken(1L, "docente@passtrack.edu", Rol.DOCENTE);

        assertThatThrownBy(() -> expiredJwtService.extractEmail(token))
            .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenConFirmaManipulada_LanzaSignatureException() {
        JwtService issuer = new JwtService(SECRET, 28800000L);
        JwtService verifier = new JwtService(OTHER_SECRET, 28800000L);

        String token = issuer.generateToken(7L, "alumno@passtrack.edu", Rol.ALUMNO);

        assertThatThrownBy(() -> verifier.extractEmail(token))
            .isInstanceOf(SignatureException.class);
    }
}
