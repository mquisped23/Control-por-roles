package com.edu.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

// =========================================================
// AUTH RESPONSE — respuesta del login y register
// =========================================================
public class AuthResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenPair {

        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;       // segundos hasta expiración del access token
        private UserInfo user;

        // Datos básicos del usuario autenticado
        // Solo lo necesario — nunca devolver password ni datos sensibles
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserInfo {
            private Long id;
            private String username;
            private String email;
            private Set<String> roles;
        }
    }

    // =========================================================
    // MENSAJE GENÉRICO — para logout y operaciones simples
    // =========================================================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String message;
    }
}