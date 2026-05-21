package com.edu.service.impl;

import com.edu.domain.dto.request.AuthRequest;
import com.edu.domain.dto.response.AuthResponse;
import com.edu.domain.entity.RefreshToken;
import com.edu.domain.entity.Role;
import com.edu.domain.entity.User;
import com.edu.repository.RefreshTokenRepository;
import com.edu.repository.RoleRepository;
import com.edu.repository.UserRepository;
import com.edu.security.util.JwtService;
import com.edu.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    @Transactional
    public AuthResponse.TokenPair register(AuthRequest.Register request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El username ya está en uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Role defaultRole = roleRepository.findByName("ROLE_TRABAJADOR")
                .orElseThrow(() -> new IllegalStateException(
                        "Rol ROLE_TRABAJADOR no encontrado — verifica las migraciones Flyway"
                ));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Set.of(defaultRole))
                .build();

        User savedUser = userRepository.save(user);
        log.info("Nuevo usuario registrado: {}", savedUser.getUsername());

        return buildTokenPair(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse.TokenPair login(AuthRequest.Login request) {

        User user = userRepository.findByUsernameOrEmail(request.getIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.getPassword()
                )
        );

        refreshTokenRepository.revokeAllUserTokens(user);

        log.info("Login exitoso: {}", user.getUsername());
        return buildTokenPair(user);
    }

    @Override
    @Transactional
    public AuthResponse.TokenPair refresh(AuthRequest.Refresh request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("Refresh token expirado o revocado");
        }

        User user = refreshToken.getUser();

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token rotado para usuario: {}", user.getUsername());
        return buildTokenPair(user);
    }

    @Override
    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        refreshTokenRepository.revokeAllUserTokens(user);
        refreshTokenRepository.deleteExpiredOrRevokedTokensByUser(user);

        log.info("Logout exitoso: {}", username);
    }

    // ---------------------------------------------------------
    // Métodos privados
    // ---------------------------------------------------------
    private AuthResponse.TokenPair buildTokenPair(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = generateAndSaveRefreshToken(user);

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AuthResponse.TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(AuthResponse.TokenPair.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .roles(roles)
                        .build())
                .build();
    }

    private String generateAndSaveRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }
}