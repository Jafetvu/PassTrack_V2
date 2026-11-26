package com.passtrack.api.controller;

import com.passtrack.api.dto.auth.AuthResponseRecord;
import com.passtrack.api.dto.auth.LoginRequestRecord;
import com.passtrack.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseRecord> login(@Valid @RequestBody LoginRequestRecord request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
