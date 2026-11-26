package com.passtrack.api.repository;

import com.passtrack.api.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    Optional<Grupo> findByClave(String clave);

    boolean existsByClave(String clave);

    List<Grupo> findByDocenteIdAndActivoTrue(Long docenteId);

    List<Grupo> findByActivoTrue();

    boolean existsByIdAndDocenteId(Long id, Long docenteId);
}
