package com.passtrack.api.controller;

import com.passtrack.api.dto.grupo.GrupoCreateRecord;
import com.passtrack.api.dto.grupo.GrupoResponseRecord;
import com.passtrack.api.dto.grupo.InscripcionCreateRecord;
import com.passtrack.api.dto.grupo.InscripcionResponseRecord;
import com.passtrack.api.security.CustomUserDetails;
import com.passtrack.api.service.GrupoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {

    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<GrupoResponseRecord> crear(@Valid @RequestBody GrupoCreateRecord payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.crearGrupo(payload));
    }

    @GetMapping
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<List<GrupoResponseRecord>> listarActivos() {
        return ResponseEntity.ok(grupoService.listarGruposActivos());
    }

    @GetMapping("/docente")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<List<GrupoResponseRecord>> listarMisGrupos(
            @AuthenticationPrincipal CustomUserDetails docente) {
        return ResponseEntity.ok(grupoService.listarGruposPorDocente(docente.getUserId()));
    }

    @PostMapping("/{grupoId}/inscripciones")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<InscripcionResponseRecord> inscribir(
            @PathVariable Long grupoId,
            @Valid @RequestBody InscripcionCreateRecord payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.inscribirAlumno(grupoId, payload));
    }

    @GetMapping("/{grupoId}/inscripciones")
    @PreAuthorize("hasAnyRole('DIRECTOR', 'DOCENTE')")
    public ResponseEntity<List<InscripcionResponseRecord>> listarInscripciones(
            @PathVariable Long grupoId,
            @AuthenticationPrincipal CustomUserDetails solicitante) {
        return ResponseEntity.ok(grupoService.listarInscripcionesPorGrupo(grupoId, solicitante));
    }
}
