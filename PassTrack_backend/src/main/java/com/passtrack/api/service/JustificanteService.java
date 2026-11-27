package com.passtrack.api.service;

import com.passtrack.api.dto.justificante.JustificanteCreateRecord;
import com.passtrack.api.dto.justificante.JustificanteResponseRecord;
import com.passtrack.api.exception.BadRequestException;
import com.passtrack.api.exception.DuplicateResourceException;
import com.passtrack.api.exception.ResourceNotFoundException;
import com.passtrack.api.model.Asistencia;
import com.passtrack.api.model.EstadoAsistencia;
import com.passtrack.api.model.EstadoJustificante;
import com.passtrack.api.model.Justificante;
import com.passtrack.api.model.Rol;
import com.passtrack.api.repository.AsistenciaRepository;
import com.passtrack.api.repository.JustificanteRepository;
import com.passtrack.api.security.CustomUserDetails;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class JustificanteService {

    private final JustificanteRepository justificanteRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final FileStorageService fileStorageService;

    public JustificanteService(
            JustificanteRepository justificanteRepository,
            AsistenciaRepository asistenciaRepository,
            FileStorageService fileStorageService) {
        this.justificanteRepository = justificanteRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public JustificanteResponseRecord solicitarJustificante(
            Long alumnoId,
            JustificanteCreateRecord payload,
            MultipartFile archivo) {
        Asistencia asistencia = asistenciaRepository.findById(payload.asistenciaId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Asistencia no encontrada con ID: " + payload.asistenciaId()));

        if (!asistencia.getAlumno().getId().equals(alumnoId)) {
            throw new AccessDeniedException("No puede solicitar justificante para una asistencia ajena");
        }

        if (asistencia.getEstado() != EstadoAsistencia.FALTA
                && asistencia.getEstado() != EstadoAsistencia.RETARDO) {
            throw new BadRequestException("Solo se pueden justificar registros con estado FALTA o RETARDO");
        }

        if (justificanteRepository.existsByAsistenciaId(asistencia.getId())) {
            throw new DuplicateResourceException("Ya existe una solicitud de justificante para esta asistencia");
        }

        String nombreArchivo = fileStorageService.storeFile(archivo);

        Justificante nuevo = Justificante.builder()
            .motivo(payload.motivo().trim())
            .evidenciaUrl(nombreArchivo)
            .estado(EstadoJustificante.PENDIENTE)
            .asistencia(asistencia)
            .build();

        return JustificanteResponseRecord.fromEntity(justificanteRepository.save(nuevo));
    }

    @Transactional(readOnly = true)
    public List<JustificanteResponseRecord> listarPendientes() {
        return justificanteRepository.findByEstado(EstadoJustificante.PENDIENTE).stream()
            .map(JustificanteResponseRecord::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public EvidenciaDownload obtenerEvidencia(Long justificanteId, CustomUserDetails solicitante) {
        Justificante justificante = justificanteRepository.findById(justificanteId)
            .orElseThrow(() -> new ResourceNotFoundException("Justificante no encontrado con ID: " + justificanteId));

        boolean esDirector = solicitante.getRol() == Rol.DIRECTOR;
        boolean esPropietario = justificante.getAsistencia().getAlumno().getId().equals(solicitante.getUserId());
        if (!esDirector && !esPropietario) {
            throw new AccessDeniedException("No tiene permisos para descargar esta evidencia");
        }

        String archivo = justificante.getEvidenciaUrl();
        Resource resource = fileStorageService.loadAsResource(archivo);
        String mediaType = fileStorageService.detectMediaType(archivo);
        return new EvidenciaDownload(resource, archivo, mediaType);
    }

    public record EvidenciaDownload(Resource resource, String filename, String mediaType) {
    }
}
