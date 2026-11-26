package com.passtrack.api.controller;

import com.passtrack.api.dto.usuario.UsuarioCreateRecord;
import com.passtrack.api.dto.usuario.UsuarioResponseRecord;
import com.passtrack.api.dto.usuario.UsuarioUpdateRecord;
import com.passtrack.api.model.Rol;
import com.passtrack.api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('DIRECTOR')")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseRecord> registrar(@Valid @RequestBody UsuarioCreateRecord payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearUsuario(payload));
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponseRecord>> consultarActivos(
            @PageableDefault(size = 20, sort = "nombre") Pageable paginacion) {
        return ResponseEntity.ok(service.listarUsuariosActivos(paginacion));
    }

    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<UsuarioResponseRecord>> consultarPorRol(@PathVariable Rol rol) {
        return ResponseEntity.ok(service.listarActivosPorRol(rol));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseRecord> consultarDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseRecord> modificar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRecord payload) {
        return ResponseEntity.ok(service.actualizarUsuario(id, payload));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        service.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
