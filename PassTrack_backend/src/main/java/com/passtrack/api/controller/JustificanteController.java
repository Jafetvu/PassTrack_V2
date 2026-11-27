package com.passtrack.api.controller;

import com.passtrack.api.dto.justificante.JustificanteCreateRecord;
import com.passtrack.api.dto.justificante.JustificanteResolucionRecord;
import com.passtrack.api.dto.justificante.JustificanteResponseRecord;
import com.passtrack.api.exception.BadRequestException;
import com.passtrack.api.security.CustomUserDetails;
import com.passtrack.api.service.JustificanteService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/justificantes")
public class JustificanteController {

    private final JustificanteService justificanteService;

    public JustificanteController(JustificanteService justificanteService) {
        this.justificanteService = justificanteService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<JustificanteResponseRecord> solicitar(
            @AuthenticationPrincipal CustomUserDetails alumno,
            @RequestParam("asistenciaId") Long asistenciaId,
            @RequestParam("motivo") String motivo,
            @RequestPart("archivo") MultipartFile archivo) {
        if (motivo == null || motivo.trim().isEmpty() || motivo.trim().length() > 500) {
            throw new BadRequestException("El motivo es obligatorio y debe tener máximo 500 caracteres");
        }
        JustificanteCreateRecord payload = new JustificanteCreateRecord(asistenciaId, motivo);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(justificanteService.solicitarJustificante(alumno.getUserId(), payload, archivo));
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<List<JustificanteResponseRecord>> listarPendientes() {
        return ResponseEntity.ok(justificanteService.listarPendientes());
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<JustificanteResponseRecord> resolver(
            @PathVariable Long id,
            @Valid @RequestBody JustificanteResolucionRecord payload) {
        return ResponseEntity.ok(justificanteService.resolverJustificante(id, payload));
    }

    @GetMapping("/{id}/evidencia")
    @PreAuthorize("hasAnyRole('DIRECTOR', 'ALUMNO')")
    public ResponseEntity<Resource> descargarEvidencia(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails solicitante) {
        JustificanteService.EvidenciaDownload download = justificanteService.obtenerEvidencia(id, solicitante);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(download.mediaType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.filename() + "\"")
            .body(download.resource());
    }
}
