-- =============================================================
-- V2__insert_initial_data.sql
-- Datos base del sistema: roles y permisos iniciales
-- Este script solo se ejecuta una vez al arrancar por primera vez
-- =============================================================

-- -------------------------------------------------------------
-- ROLES BASE
-- -------------------------------------------------------------
INSERT INTO roles (name, description) VALUES
                                          ('ROLE_ADMIN',      'Administrador del sistema con acceso total'),
                                          ('ROLE_TRABAJADOR', 'Trabajador con acceso limitado a sus recursos');

-- -------------------------------------------------------------
-- PERMISSIONS BASE
-- Nomenclatura: recurso:accion
-- -------------------------------------------------------------
INSERT INTO permissions (name, description) VALUES
                                                -- Permisos sobre usuarios
                                                ('user:read',       'Ver información de usuarios'),
                                                ('user:write',      'Crear y editar usuarios'),
                                                ('user:delete',     'Eliminar usuarios'),
                                                -- Permisos sobre roles
                                                ('role:read',       'Ver roles del sistema'),
                                                ('role:write',      'Crear y editar roles'),
                                                -- Permisos propios del trabajador
                                                ('worker:read',     'Ver sus propios datos como trabajador'),
                                                ('worker:write',    'Editar sus propios datos como trabajador');

-- -------------------------------------------------------------
-- ASIGNACIÓN DE PERMISSIONS A ROLES
-- ADMIN tiene todos los permisos
-- TRABAJADOR solo tiene acceso a sus propios recursos
-- -------------------------------------------------------------

-- ADMIN: todos los permisos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN';

-- TRABAJADOR: solo sus permisos específicos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN ('worker:read', 'worker:write')
WHERE r.name = 'ROLE_TRABAJADOR';