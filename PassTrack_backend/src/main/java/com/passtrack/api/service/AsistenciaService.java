package com.passtrack.api.service;

import com.passtrack.api.dto.asistencia.AsistenciaResponseRecord;
import com.passtrack.api.dto.asistencia.PaseListaItemRecord;
import com.passtrack.api.dto.asistencia.RegistroAsistenciaBulkRecord;
import com.passtrack.api.dto.asistencia.ResumenAsistenciaAlumnoRecord;
import com.passtrack.api.exception.BadRequestException;
import com.passtrack.api.exception.DuplicateResourceException;
import com.passtrack.api.exception.ResourceNotFoundException;
import com.passtrack.api.model.Asistencia;
import com.passtrack.api.model.EstadoAsistencia;
import com.passtrack.api.model.Grupo;
import com.passtrack.api.model.Inscripcion;
import com.passtrack.api.model.Justificante;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.AsistenciaRepository;
import com.passtrack.api.repository.GrupoRepository;
import com.passtrack.api.repository.InscripcionRepository;
import com.passtrack.api.repository.JustificanteRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final GrupoRepository grupoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final JustificanteRepository justificanteRepository;

    public AsistenciaService(
            AsistenciaRepository asistenciaRepository,
            GrupoRepository grupoRepository,
            InscripcionRepository inscripcionRepository,
            JustificanteRepository justificanteRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.grupoRepository = grupoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.justificanteRepository = justificanteRepository;
    }

    @Transactional
    public List<AsistenciaResponseRecord> registrarPaseLista(
            Long grupoId,
            Long docenteId,
            RegistroAsistenciaBulkRecord payload) {
        Grupo grupo = obtenerGrupoValidandoTitularidad(grupoId, docenteId);

        LocalDate hoy = LocalDate.now();
        if (!payload.fecha().equals(hoy)) {
            throw new BadRequestException("Solo se permite registrar el pase de lista para la fecha actual (" + hoy + ")");
        }

        if (asistenciaRepository.existsByGrupoIdAndFecha(grupoId, hoy)) {
            throw new DuplicateResourceException("Ya existe un pase de lista registrado hoy para este grupo");
        }

        Map<Long, Usuario> alumnosInscritos = inscripcionRepository.findByGrupoId(grupoId).stream()
            .map(Inscripcion::getAlumno)
            .collect(Collectors.toMap(Usuario::getId, Function.identity()));

        Set<Long> procesados = new HashSet<>();
        List<Asistencia> registros = new ArrayList<>(payload.lista().size());

        for (PaseListaItemRecord item : payload.lista()) {
            if (!procesados.add(item.alumnoId())) {
                throw new BadRequestException("Alumno duplicado en el pase de lista: ID " + item.alumnoId());
            }
            Usuario alumno = alumnosInscritos.get(item.alumnoId());
            if (alumno == null) {
                throw new BadRequestException("El alumno con ID " + item.alumnoId() + " no está inscrito en el grupo");
            }
            registros.add(Asistencia.builder()
                .fecha(hoy)
                .estado(item.estado())
                .alumno(alumno)
                .grupo(grupo)
                .build());
        }

        return asistenciaRepository.saveAll(registros).stream()
            .map(a -> AsistenciaResponseRecord.fromEntity(a, false, null))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AsistenciaResponseRecord> obtenerAsistenciasHoy(Long grupoId, Long docenteId) {
        obtenerGrupoValidandoTitularidad(grupoId, docenteId);

        List<Asistencia> hoy = asistenciaRepository.findByGrupoIdAndFecha(grupoId, LocalDate.now());
        return hoy.stream()
            .map(a -> {
                Justificante j = justificanteRepository.findByAsistenciaId(a.getId()).orElse(null);
                return AsistenciaResponseRecord.fromEntity(a, j != null, j != null ? j.getEstado() : null);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ResumenAsistenciaAlumnoRecord> obtenerResumenPorAlumno(Long alumnoId) {
        List<Inscripcion> inscripciones = inscripcionRepository.findByAlumnoId(alumnoId);
        List<Asistencia> todas = asistenciaRepository.findByAlumnoId(alumnoId);
        Map<Long, Justificante> justificantesPorAsistencia = justificanteRepository.findByAsistenciaAlumnoId(alumnoId)
            .stream()
            .collect(Collectors.toMap(j -> j.getAsistencia().getId(), Function.identity(), (a, b) -> a));

        Map<Long, List<Asistencia>> porGrupo = new HashMap<>();
        for (Asistencia a : todas) {
            porGrupo.computeIfAbsent(a.getGrupo().getId(), k -> new ArrayList<>()).add(a);
        }

        List<ResumenAsistenciaAlumnoRecord> resultado = new ArrayList<>(inscripciones.size());
        for (Inscripcion ins : inscripciones) {
            Grupo grupo = ins.getGrupo();
            List<Asistencia> listaGrupo = porGrupo.getOrDefault(grupo.getId(), List.of());

            long presentes = contar(listaGrupo, EstadoAsistencia.PRESENTE);
            long faltas = contar(listaGrupo, EstadoAsistencia.FALTA);
            long retardos = contar(listaGrupo, EstadoAsistencia.RETARDO);
            long justificados = contar(listaGrupo, EstadoAsistencia.JUSTIFICADO);
            long total = listaGrupo.size();
            double porcentaje = total == 0
                ? 100.0
                : Math.round(((presentes + retardos + justificados) * 1000.0) / total) / 10.0;

            List<AsistenciaResponseRecord> historial = listaGrupo.stream()
                .sorted(Comparator.comparing(Asistencia::getFecha).reversed())
                .map(a -> {
                    Justificante j = justificantesPorAsistencia.get(a.getId());
                    return AsistenciaResponseRecord.fromEntity(a, j != null, j != null ? j.getEstado() : null);
                })
                .toList();

            resultado.add(new ResumenAsistenciaAlumnoRecord(
                grupo.getId(),
                grupo.getClave(),
                grupo.getNombre(),
                total,
                presentes,
                faltas,
                retardos,
                justificados,
                porcentaje,
                historial
            ));
        }

        return resultado;
    }

    private Grupo obtenerGrupoValidandoTitularidad(Long grupoId, Long docenteId) {
        Grupo grupo = grupoRepository.findById(grupoId)
            .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado con ID: " + grupoId));
        if (!grupo.getDocente().getId().equals(docenteId)) {
            throw new AccessDeniedException("No es el docente titular asignado a este grupo");
        }
        return grupo;
    }

    private long contar(List<Asistencia> lista, EstadoAsistencia estado) {
        return lista.stream().filter(a -> a.getEstado() == estado).count();
    }
}
