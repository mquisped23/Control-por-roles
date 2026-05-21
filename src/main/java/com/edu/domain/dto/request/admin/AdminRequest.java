package com.edu.domain.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AdminRequest {

    // ---------------------------------------------------------
    // CREATE USER — ADMIN crea un usuario con rol seleccionable
    // El front envía el rol desde un select: ADMIN o TRABAJADOR
    // ---------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUser {

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

        // El front envía "ROLE_ADMIN" o "ROLE_TRABAJADOR" desde un select
        @NotNull(message = "El rol es obligatorio")
        private RoleName role;

        // Enum con los roles disponibles para seleccionar
        public enum RoleName {
            ROLE_ADMIN,
            ROLE_TRABAJADOR
        }
    }
}