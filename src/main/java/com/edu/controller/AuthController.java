package com.edu.controller;


import com.edu.domain.dto.request.AuthRequest;
import com.edu.domain.dto.response.AuthResponse;
import com.edu.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/v1/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse.TokenPair> register(
            @Valid @RequestBody AuthRequest.Register request
    ) {
        AuthResponse.TokenPair response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/v1/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse.TokenPair> login(
            @Valid @RequestBody AuthRequest.Login request
    ) {
        AuthResponse.TokenPair response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // POST /api/v1/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse.TokenPair> refresh(
            @Valid @RequestBody AuthRequest.Refresh request
    ) {
        AuthResponse.TokenPair response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    // POST /api/v1/auth/logout
    // @AuthenticationPrincipal inyecta el usuario del SecurityContext actual
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse.Message> logout(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(
                AuthResponse.Message.builder()
                        .message("Sesión cerrada correctamente")
                        .build()
        );
    }
}
