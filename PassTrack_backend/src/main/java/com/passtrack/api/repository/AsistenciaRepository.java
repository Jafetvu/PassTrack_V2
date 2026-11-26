package com.passtrack.api.repository;

import com.passtrack.api.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findByGrupoIdAndFecha(Long grupoId, LocalDate fecha);

    boolean existsByGrupoIdAndFecha(Long grupoId, LocalDate fecha);

    Optional<Asistencia> findByAlumnoIdAndGrupoIdAndFecha(Long alumnoId, Long grupoId, LocalDate fecha);

    List<Asistencia> findByAlumnoId(Long alumnoId);

    List<Asistencia> findByAlumnoIdAndGrupoId(Long alumnoId, Long grupoId);
}
