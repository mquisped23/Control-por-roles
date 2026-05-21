package com.edu.domain.dto.response.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

public class WorkerResponse {

    // ---------------------------------------------------------
    // PERFIL DEL TRABAJADOR
    // Solo sus propios datos — sin información sensible
    // ---------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private Long id;
        private String username;
        private String email;
        private Set<String> roles;
        private Set<String> permissions;
        private LocalDateTime createdAt;
    }

    // ---------------------------------------------------------
    // MENSAJE GENÉRICO
    // ---------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String message;
    }
}