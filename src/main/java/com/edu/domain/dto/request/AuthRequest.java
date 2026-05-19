package com.edu.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// =========================================================
// REGISTER REQUEST
// =========================================================
public class AuthRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Register {

        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
        private String username;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener mínimo 8 caracteres")
        private String password;
    }

    // =========================================================
    // LOGIN REQUEST
    // El usuario puede identificarse con username o email
    // =========================================================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {

        @NotBlank(message = "El identificador es obligatorio")
        // Acepta username o email — el service resuelve cuál es
        private String identifier;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    // =========================================================
    // REFRESH TOKEN REQUEST
    // =========================================================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Refresh {

        @NotBlank(message = "El refresh token es obligatorio")
        private String refreshToken;
    }
}