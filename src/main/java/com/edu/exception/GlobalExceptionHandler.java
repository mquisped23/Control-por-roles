package com.edu.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // ERRORES DE VALIDACIÓN — @Valid falla en los DTOs
    // Devuelve 400 con detalle de cada campo inválido
    // =========================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        // Agrupa todos los errores de validación por campo
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        field -> field.getDefaultMessage() != null
                                ? field.getDefaultMessage()
                                : "Valor inválido",
                        // Si hay dos errores en el mismo campo, quedarse con el primero
                        (existing, replacement) -> existing
                ));

        log.warn("Error de validación en {}: {}", request.getServletPath(), fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(
                        HttpStatus.BAD_REQUEST,
                        "Error de validación",
                        "Los datos enviados contienen errores",
                        request.getServletPath(),
                        fieldErrors
                ));
    }

    // =========================================================
    // CREDENCIALES INVÁLIDAS — login con password incorrecto
    // =========================================================
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        // Mensaje genérico — no revelar si el usuario existe o fue la contraseña
        log.warn("Intento de login fallido en: {}", request.getServletPath());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(buildBody(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciales inválidas",
                        "Usuario o contraseña incorrectos",
                        request.getServletPath(),
                        null
                ));
    }

    // =========================================================
    // CUENTA DESHABILITADA
    // =========================================================
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(
            DisabledException ex,
            HttpServletRequest request
    ) {
        log.warn("Intento de acceso con cuenta deshabilitada en: {}", request.getServletPath());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(buildBody(
                        HttpStatus.FORBIDDEN,
                        "Cuenta deshabilitada",
                        "Tu cuenta está deshabilitada, contacta al administrador",
                        request.getServletPath(),
                        null
                ));
    }

    // =========================================================
    // CUENTA BLOQUEADA
    // =========================================================
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, Object>> handleLocked(
            LockedException ex,
            HttpServletRequest request
    ) {
        log.warn("Intento de acceso con cuenta bloqueada en: {}", request.getServletPath());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(buildBody(
                        HttpStatus.FORBIDDEN,
                        "Cuenta bloqueada",
                        "Tu cuenta está bloqueada temporalmente",
                        request.getServletPath(),
                        null
                ));
    }

    // =========================================================
    // ARGUMENTO INVÁLIDO — lanzado manualmente en los services
    // Ejemplo: "El username ya está en uso"
    // =========================================================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Argumento inválido en {}: {}", request.getServletPath(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(
                        HttpStatus.BAD_REQUEST,
                        "Solicitud inválida",
                        ex.getMessage(),
                        request.getServletPath(),
                        null
                ));
    }

    // =========================================================
    // ESTADO ILEGAL — lanzado cuando falta configuración crítica
    // Ejemplo: "Rol ROLE_TRABAJADOR no encontrado"
    // =========================================================
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        log.error("Estado ilegal en {}: {}", request.getServletPath(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error interno",
                        "Ocurrió un error interno, contacta al administrador",
                        request.getServletPath(),
                        null
                ));
    }

    // =========================================================
    // ACCESO DENEGADO — usuario autenticado sin permisos
    // Spring Security lo lanza antes de llegar aquí normalmente,
    // pero lo capturamos por si se lanza manualmente en el código
    // =========================================================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Acceso denegado en {}: {}", request.getServletPath(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(buildBody(
                        HttpStatus.FORBIDDEN,
                        "Acceso denegado",
                        "No tienes permisos para realizar esta acción",
                        request.getServletPath(),
                        null
                ));
    }

    // =========================================================
    // CATCH-ALL — cualquier excepción no controlada
    // Nunca expone el stack trace al cliente
    // =========================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex,
            HttpServletRequest request
    ) {
        // Log completo con stack trace solo en el servidor
        log.error("Error no controlado en {}: {}", request.getServletPath(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error interno del servidor",
                        "Ocurrió un error inesperado, intenta más tarde",
                        request.getServletPath(),
                        null
                ));
    }

    // =========================================================
    // METODO PRIVADO — construye el cuerpo de respuesta uniforme
    // =========================================================
    private Map<String, Object> buildBody(
            HttpStatus status,
            String error,
            String message,
            String path,
            Map<String, String> details
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);

        // Solo incluimos details si hay errores de campo (validación)
        if (details != null && !details.isEmpty()) {
            body.put("details", details);
        }

        return body;
    }
}