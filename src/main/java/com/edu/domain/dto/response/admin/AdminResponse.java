package com.edu.domain.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class AdminResponse {

    // ---------------------------------------------------------
    // DETALLE DE UN USUARIO
    // ---------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetail {
        private Long id;
        private String username;
        private String email;
        private Boolean enabled;
        private Set<String> roles;
        private Set<String> permissions;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // ---------------------------------------------------------
    // LISTA DE USUARIOS
    // ---------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserList {
        private List<UserDetail> users;
        private Integer total;
    }

    // ---------------------------------------------------------
    // DETALLE DE UN ROL
    // ---------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDetail {
        private Long id;
        private String name;
        private String description;
        private Set<String> permissions;
    }

    // ---------------------------------------------------------
    // LISTA DE ROLES
    // ---------------------------------------------------------
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleList {
        private List<RoleDetail> roles;
        private Integer total;
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
