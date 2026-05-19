-- =============================================================
-- V1__create_security_tables.sql
-- Creación del esquema base de seguridad
-- Roles, Permissions, Users y sus relaciones
-- =============================================================

-- -------------------------------------------------------------
-- ROLES
-- ADMIN y TRABAJADOR son los roles base del sistema
-- -------------------------------------------------------------
CREATE TABLE roles (
                       id          BIGINT          NOT NULL AUTO_INCREMENT,
                       name        VARCHAR(50)     NOT NULL,
                       description VARCHAR(255),
                       created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- PERMISSIONS
-- Permisos granulares por recurso y acción: recurso:accion
-- Ejemplos: user:read, user:write, admin:read
-- -------------------------------------------------------------
CREATE TABLE permissions (
                             id          BIGINT          NOT NULL AUTO_INCREMENT,
                             name        VARCHAR(100)    NOT NULL,
                             description VARCHAR(255),
                             created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (id),
                             CONSTRAINT uk_permissions_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- USERS
-- -------------------------------------------------------------
CREATE TABLE users (
                       id          BIGINT          NOT NULL AUTO_INCREMENT,
                       username    VARCHAR(50)     NOT NULL,
                       email       VARCHAR(100)    NOT NULL,
                       password    VARCHAR(255)    NOT NULL,
                       enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
                       created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (id),
                       CONSTRAINT uk_users_username    UNIQUE (username),
                       CONSTRAINT uk_users_email       UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- USER_ROLES — relación muchos a muchos entre users y roles
-- -------------------------------------------------------------
CREATE TABLE user_roles (
                            user_id     BIGINT  NOT NULL,
                            role_id     BIGINT  NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
                                REFERENCES users (id) ON DELETE CASCADE,
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
                                REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- ROLE_PERMISSIONS — relación muchos a muchos entre roles y permissions
-- -------------------------------------------------------------
CREATE TABLE role_permissions (
                                  role_id         BIGINT  NOT NULL,
                                  permission_id   BIGINT  NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id)
                                      REFERENCES roles (id) ON DELETE CASCADE,
                                  CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id)
                                      REFERENCES permissions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- REFRESH_TOKENS
-- Persiste los refresh tokens para poder revocarlos
-- Un user puede tener múltiples tokens activos (varios dispositivos)
-- -------------------------------------------------------------
CREATE TABLE refresh_tokens (
                                id          BIGINT          NOT NULL AUTO_INCREMENT,
                                token       VARCHAR(512)    NOT NULL,
                                user_id     BIGINT          NOT NULL,
                                expires_at  DATETIME        NOT NULL,
                                revoked     BOOLEAN         NOT NULL DEFAULT FALSE,
                                created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (id),
                                CONSTRAINT uk_refresh_tokens_token  UNIQUE (token),
                                CONSTRAINT fk_refresh_tokens_user   FOREIGN KEY (user_id)
                                    REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;