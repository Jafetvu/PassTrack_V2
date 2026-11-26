package com.passtrack.api.service;

import com.passtrack.api.dto.auth.AuthResponseRecord;
import com.passtrack.api.dto.auth.LoginRequestRecord;
import com.passtrack.api.security.CustomUserDetails;
import com.passtrack.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponseRecord login(LoginRequestRecord request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return new AuthResponseRecord(
            token,
            userDetails.getUserId(),
            userDetails.getNombre(),
            userDetails.getEmail(),
            userDetails.getRol()
        );
    }
}
