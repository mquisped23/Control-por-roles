package com.edu.controller.worker;


import com.edu.domain.dto.request.worker.WorkerRequest;
import com.edu.domain.dto.response.worker.WorkerResponse;
import com.edu.service.worker.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/worker")
@RequiredArgsConstructor
// Doble protección: SecurityConfig bloquea por ruta Y @PreAuthorize por método
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
public class WorkerController {

    private final WorkerService workerService;

    // ---------------------------------------------------------
    // GET /api/v1/worker/me
    // El trabajador ve su propio perfil
    // ---------------------------------------------------------
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('worker:read')")
    public ResponseEntity<WorkerResponse.Profile> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.debug("Trabajador '{}' consultando su perfil", userDetails.getUsername());
        return ResponseEntity.ok(workerService.getProfile(userDetails.getUsername()));
    }

    // ---------------------------------------------------------
    // PATCH /api/v1/worker/me
    // El trabajador actualiza su propia información
    // Solo puede editar sus propios datos — nunca los de otro usuario
    // ---------------------------------------------------------
    @PatchMapping("/me")
    @PreAuthorize("hasAuthority('worker:write')")
    public ResponseEntity<WorkerResponse.Profile> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkerRequest.UpdateProfile request
    ) {
        log.info("Trabajador '{}' actualizando su perfil", userDetails.getUsername());
        return ResponseEntity.ok(
                workerService.updateProfile(userDetails.getUsername(), request)
        );
    }

    // ---------------------------------------------------------
    // PATCH /api/v1/worker/me/password
    // El trabajador cambia su propia contraseña
    // ---------------------------------------------------------
    @PatchMapping("/me/password")
    @PreAuthorize("hasAuthority('worker:write')")
    public ResponseEntity<WorkerResponse.Message> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkerRequest.ChangePassword request
    ) {
        log.info("Trabajador '{}' cambiando contraseña", userDetails.getUsername());
        workerService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(
                WorkerResponse.Message.builder()
                        .message("Contraseña actualizada correctamente")
                        .build()
        );
    }
}