package com.passtrack.api.service;

import com.passtrack.api.dto.usuario.UsuarioCreateRecord;
import com.passtrack.api.dto.usuario.UsuarioResponseRecord;
import com.passtrack.api.dto.usuario.UsuarioUpdateRecord;
import com.passtrack.api.exception.DuplicateResourceException;
import com.passtrack.api.exception.ResourceNotFoundException;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseRecord crearUsuario(UsuarioCreateRecord request) {
        String emailNormalizado = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new DuplicateResourceException("Ya existe un usuario registrado con el correo: " + emailNormalizado);
        }

        Usuario usuario = Usuario.builder()
            .nombre(request.nombre().trim())
            .email(emailNormalizado)
            .passwordHash(passwordEncoder.encode(request.password()))
            .rol(request.rol())
            .activo(true)
            .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return UsuarioResponseRecord.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseRecord> listarUsuariosActivos(Pageable pageable) {
        return usuarioRepository.findByActivoTrue(pageable)
            .map(UsuarioResponseRecord::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseRecord> listarActivosPorRol(Rol rol) {
        return usuarioRepository.findByRolAndActivoTrue(rol).stream()
            .map(UsuarioResponseRecord::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseRecord obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return UsuarioResponseRecord.fromEntity(usuario);
    }

    @Transactional
    public UsuarioResponseRecord actualizarUsuario(Long id, UsuarioUpdateRecord request) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setNombre(request.nombre().trim());
        usuario.setRol(request.rol());
        return UsuarioResponseRecord.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public void desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
}
