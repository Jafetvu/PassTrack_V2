package com.passtrack.api.config;

import com.passtrack.api.model.Grupo;
import com.passtrack.api.model.Inscripcion;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.GrupoRepository;
import com.passtrack.api.repository.InscripcionRepository;
import com.passtrack.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "passtrack.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UsuarioRepository usuarioRepository,
            GrupoRepository grupoRepository,
            InscripcionRepository inscripcionRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository = grupoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        asegurarUsuario("Dr. Roberto Gómez", "director@passtrack.edu", "Director123!", Rol.DIRECTOR);
        Usuario docente = asegurarUsuario("Profa. Elena Ruiz", "docente@passtrack.edu", "Docente123!", Rol.DOCENTE);
        Usuario alumno1 = asegurarUsuario("Carlos Méndez", "alumno1@passtrack.edu", "Alumno123!", Rol.ALUMNO);
        Usuario alumno2 = asegurarUsuario("Ana Morales", "alumno2@passtrack.edu", "Alumno123!", Rol.ALUMNO);

        Grupo grupo = grupoRepository.findByClave("MAT-101")
            .orElseGet(() -> grupoRepository.save(Grupo.builder()
                .clave("MAT-101")
                .nombre("Matemáticas I")
                .docente(docente)
                .activo(true)
                .build()));

        asegurarInscripcion(alumno1, grupo);
        asegurarInscripcion(alumno2, grupo);

        log.info("Datos semilla verificados correctamente (usuarios, grupo MAT-101 e inscripciones).");
    }

    private Usuario asegurarUsuario(String nombre, String email, String rawPassword, Rol rol) {
        return usuarioRepository.findByEmail(email)
            .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                .nombre(nombre)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .rol(rol)
                .activo(true)
                .build()));
    }

    private void asegurarInscripcion(Usuario alumno, Grupo grupo) {
        if (!inscripcionRepository.existsByAlumnoIdAndGrupoId(alumno.getId(), grupo.getId())) {
            inscripcionRepository.save(Inscripcion.builder()
                .alumno(alumno)
                .grupo(grupo)
                .build());
        }
    }
}
