package com.passtrack.api.config;

import com.passtrack.api.repository.GrupoRepository;
import com.passtrack.api.repository.InscripcionRepository;
import com.passtrack.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DataSeederTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void run_EsIdempotenteYCreaDatosIniciales() {
        DataSeeder seeder = new DataSeeder(
            usuarioRepository,
            grupoRepository,
            inscripcionRepository,
            passwordEncoder
        );

        seeder.run();
        seeder.run();

        assertThat(usuarioRepository.findByEmail("director@passtrack.edu")).isPresent();
        assertThat(usuarioRepository.findByEmail("docente@passtrack.edu")).isPresent();
        assertThat(usuarioRepository.findByEmail("alumno1@passtrack.edu")).isPresent();
        assertThat(usuarioRepository.findByEmail("alumno2@passtrack.edu")).isPresent();

        var grupo = grupoRepository.findByClave("MAT-101").orElseThrow();
        assertThat(inscripcionRepository.countByGrupoId(grupo.getId())).isEqualTo(2L);
    }
}
