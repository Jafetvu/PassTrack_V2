package com.passtrack.api.repository;

import com.passtrack.api.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    List<Inscripcion> findByGrupoId(Long grupoId);

    List<Inscripcion> findByAlumnoId(Long alumnoId);

    Optional<Inscripcion> findByAlumnoIdAndGrupoId(Long alumnoId, Long grupoId);

    boolean existsByAlumnoIdAndGrupoId(Long alumnoId, Long grupoId);

    long countByGrupoId(Long grupoId);
}
