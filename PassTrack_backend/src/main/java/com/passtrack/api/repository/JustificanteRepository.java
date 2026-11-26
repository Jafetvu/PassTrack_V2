package com.passtrack.api.repository;

import com.passtrack.api.model.EstadoJustificante;
import com.passtrack.api.model.Justificante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JustificanteRepository extends JpaRepository<Justificante, Long> {

    Optional<Justificante> findByAsistenciaId(Long asistenciaId);

    boolean existsByAsistenciaId(Long asistenciaId);

    List<Justificante> findByEstado(EstadoJustificante estado);

    List<Justificante> findByAsistenciaAlumnoId(Long alumnoId);
}
