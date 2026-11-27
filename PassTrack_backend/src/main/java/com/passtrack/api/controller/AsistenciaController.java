package com.passtrack.api.controller;

import com.passtrack.api.dto.asistencia.AsistenciaResponseRecord;
import com.passtrack.api.dto.asistencia.RegistroAsistenciaBulkRecord;
import com.passtrack.api.dto.asistencia.ResumenAsistenciaAlumnoRecord;
import com.passtrack.api.security.CustomUserDetails;
import com.passtrack.api.service.AsistenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping("/grupo/{id}")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<List<AsistenciaResponseRecord>> registrarPaseLista(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails docente,
            @Valid @RequestBody RegistroAsistenciaBulkRecord payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(asistenciaService.registrarPaseLista(id, docente.getUserId(), payload));
    }

    @GetMapping("/grupo/{id}/hoy")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<List<AsistenciaResponseRecord>> consultarHoy(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails docente) {
        return ResponseEntity.ok(asistenciaService.obtenerAsistenciasHoy(id, docente.getUserId()));
    }

    @GetMapping("/mis-asistencias")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<List<ResumenAsistenciaAlumnoRecord>> consultarMisAsistencias(
            @AuthenticationPrincipal CustomUserDetails alumno) {
        return ResponseEntity.ok(asistenciaService.obtenerResumenPorAlumno(alumno.getUserId()));
    }
}
