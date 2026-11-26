package com.passtrack.api.service;

import com.passtrack.api.dto.grupo.GrupoCreateRecord;
import com.passtrack.api.dto.grupo.GrupoResponseRecord;
import com.passtrack.api.dto.grupo.InscripcionCreateRecord;
import com.passtrack.api.dto.grupo.InscripcionResponseRecord;
import com.passtrack.api.exception.BadRequestException;
import com.passtrack.api.exception.DuplicateResourceException;
import com.passtrack.api.exception.ResourceNotFoundException;
import com.passtrack.api.model.Grupo;
import com.passtrack.api.model.Inscripcion;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.GrupoRepository;
import com.passtrack.api.repository.InscripcionRepository;
import com.passtrack.api.repository.UsuarioRepository;
import com.passtrack.api.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;

    public GrupoService(
            GrupoRepository grupoRepository,
            UsuarioRepository usuarioRepository,
            InscripcionRepository inscripcionRepository) {
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional
    public GrupoResponseRecord crearGrupo(GrupoCreateRecord payload) {
        String claveLimpia = payload.clave().trim().toUpperCase();
        if (grupoRepository.existsByClave(claveLimpia)) {
            throw new DuplicateResourceException("Ya existe un grupo con la clave: " + claveLimpia);
        }

        Usuario docente = usuarioRepository.findById(payload.docenteId())
            .filter(u -> Boolean.TRUE.equals(u.getActivo()) && u.getRol() == Rol.DOCENTE)
            .orElseThrow(() -> new BadRequestException("El docente titular no existe, está inactivo o no posee rol DOCENTE"));

        Grupo nuevo = Grupo.builder()
            .clave(claveLimpia)
            .nombre(payload.nombre().trim())
            .docente(docente)
            .activo(true)
            .build();

        return GrupoResponseRecord.fromEntity(grupoRepository.save(nuevo), 0L);
    }

    @Transactional(readOnly = true)
    public List<GrupoResponseRecord> listarGruposActivos() {
        return grupoRepository.findByActivoTrue().stream()
            .map(g -> GrupoResponseRecord.fromEntity(g, inscripcionRepository.countByGrupoId(g.getId())))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<GrupoResponseRecord> listarGruposPorDocente(Long docenteId) {
        return grupoRepository.findByDocenteIdAndActivoTrue(docenteId).stream()
            .map(g -> GrupoResponseRecord.fromEntity(g, inscripcionRepository.countByGrupoId(g.getId())))
            .toList();
    }

    @Transactional
    public InscripcionResponseRecord inscribirAlumno(Long grupoId, InscripcionCreateRecord payload) {
        Grupo grupo = grupoRepository.findById(grupoId)
            .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado con ID: " + grupoId));

        if (!Boolean.TRUE.equals(grupo.getActivo())) {
            throw new BadRequestException("No se puede inscribir alumnos en un grupo inactivo");
        }

        Usuario alumno = usuarioRepository.findById(payload.alumnoId())
            .filter(u -> Boolean.TRUE.equals(u.getActivo()) && u.getRol() == Rol.ALUMNO)
            .orElseThrow(() -> new BadRequestException("El alumno indicado no existe, está inactivo o no posee rol ALUMNO"));

        if (inscripcionRepository.existsByAlumnoIdAndGrupoId(alumno.getId(), grupo.getId())) {
            throw new DuplicateResourceException("El alumno ya se encuentra inscrito en este grupo");
        }

        Inscripcion registro = Inscripcion.builder()
            .alumno(alumno)
            .grupo(grupo)
            .build();

        return InscripcionResponseRecord.fromEntity(inscripcionRepository.save(registro));
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponseRecord> listarInscripcionesPorGrupo(Long grupoId, CustomUserDetails solicitante) {
        Grupo grupo = grupoRepository.findById(grupoId)
            .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado con ID: " + grupoId));

        if (solicitante.getRol() == Rol.DOCENTE && !grupo.getDocente().getId().equals(solicitante.getUserId())) {
            throw new AccessDeniedException("No tiene permisos para consultar inscripciones de un grupo ajeno");
        }

        return inscripcionRepository.findByGrupoId(grupoId).stream()
            .map(InscripcionResponseRecord::fromEntity)
            .toList();
    }
}
