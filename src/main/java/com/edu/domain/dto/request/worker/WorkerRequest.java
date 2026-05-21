package com.edu.domain.dto.request.worker;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class WorkerRequest {

    // ---------------------------------------------------------
    // UPDATE PROFILE — actualizar datos propios
    // ---------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfile {

        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
        private String username;

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        private String email;
    }

    // ---------------------------------------------------------
    // CHANGE PASSWORD — cambiar contraseña propia
    // ---------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePassword {

        @NotBlank(message = "La contraseña actual es obligatoria")
        private String currentPassword;

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La nueva contraseña debe tener mínimo 8 caracteres")
        private String newPassword;

        @NotBlank(message = "La confirmación de contraseña es obligatoria")
        private String confirmPassword;
    }
}