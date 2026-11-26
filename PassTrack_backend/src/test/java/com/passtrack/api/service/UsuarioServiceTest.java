package com.passtrack.api.service;

import com.passtrack.api.dto.usuario.UsuarioCreateRecord;
import com.passtrack.api.dto.usuario.UsuarioResponseRecord;
import com.passtrack.api.exception.DuplicateResourceException;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void crearUsuario_DatosValidos_CifraPasswordConBCryptYRetornaDto() {
        UsuarioCreateRecord request = new UsuarioCreateRecord(
            "Mtra. Laura Morales",
            "docente@passtrack.edu",
            "Docente123!",
            Rol.DOCENTE
        );

        when(usuarioRepository.existsByEmail("docente@passtrack.edu")).thenReturn(false);
        when(passwordEncoder.encode("Docente123!")).thenReturn("$2a$10$encodedHash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(10L);
            u.setCreatedAt(Instant.now());
            return u;
        });

        UsuarioResponseRecord response = usuarioService.crearUsuario(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$10$encodedHash");
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.email()).isEqualTo("docente@passtrack.edu");
        assertThat(response.rol()).isEqualTo(Rol.DOCENTE);
        assertThat(response.activo()).isTrue();
    }

    @Test
    void crearUsuario_EmailDuplicado_LanzaDuplicateResourceException() {
        UsuarioCreateRecord request = new UsuarioCreateRecord(
            "Duplicado",
            "existente@passtrack.edu",
            "Password123!",
            Rol.ALUMNO
        );

        when(usuarioRepository.existsByEmail("existente@passtrack.edu")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crearUsuario(request))
            .isInstanceOf(DuplicateResourceException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void desactivarUsuario_AplicaBorradoLogicoSinEliminarFisicamente() {
        Usuario usuario = Usuario.builder()
            .id(5L)
            .nombre("Alumno Test")
            .email("alumno@passtrack.edu")
            .passwordHash("hash")
            .rol(Rol.ALUMNO)
            .activo(true)
            .build();

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        usuarioService.desactivarUsuario(5L);

        assertThat(usuario.getActivo()).isFalse();
        verify(usuarioRepository).save(usuario);
        verify(usuarioRepository, never()).delete(any());
        verify(usuarioRepository, never()).deleteById(any());
    }
}
