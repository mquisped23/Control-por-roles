package com.edu.controller.admin;



import com.edu.domain.dto.request.admin.AdminRequest;
import com.edu.domain.dto.response.admin.AdminResponse;
import com.edu.service.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
// Doble protección: SecurityConfig bloquea por ruta Y @PreAuthorize por método
// Si alguien modifica el SecurityConfig accidentalmente, @PreAuthorize sigue protegiendo
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ---------------------------------------------------------
    // GET /api/v1/admin/users
    // Lista todos los usuarios del sistema
    // ---------------------------------------------------------
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<AdminResponse.UserList> getAllUsers() {
        log.debug("ADMIN solicitó lista de usuarios");
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // ---------------------------------------------------------
    // GET /api/v1/admin/users/{id}
    // Obtiene un usuario específico por ID
    // ---------------------------------------------------------
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<AdminResponse.UserDetail> getUserById(@PathVariable Long id) {
        log.debug("ADMIN solicitó usuario con id: {}", id);
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    //Metodo para crear un usuario de admin o worker
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AdminResponse.UserDetail> createUser(
            @Valid @RequestBody AdminRequest.CreateUser request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        log.info("ADMIN '{}' creando usuario '{}' con rol '{}'",
                currentUser.getUsername(), request.getUsername(), request.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createUser(request));
    }

    // ---------------------------------------------------------
    // PATCH /api/v1/admin/users/{id}/promote
    // Promueve a un usuario al rol ADMIN
    // Solo un ADMIN puede crear otro ADMIN
    // ---------------------------------------------------------
    @PatchMapping("/users/{id}/promote")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AdminResponse.UserDetail> promoteToAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        log.info("ADMIN '{}' promoviendo usuario id: {} a ADMIN", currentUser.getUsername(), id);
        return ResponseEntity.ok(adminService.promoteToAdmin(id));
    }

    // ---------------------------------------------------------
    // PATCH /api/v1/admin/users/{id}/disable
    // Deshabilita una cuenta de usuario
    // ---------------------------------------------------------
    @PatchMapping("/users/{id}/disable")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AdminResponse.UserDetail> disableUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        log.info("ADMIN '{}' deshabilitando usuario id: {}", currentUser.getUsername(), id);
        return ResponseEntity.ok(adminService.disableUser(id));
    }

    // ---------------------------------------------------------
    // PATCH /api/v1/admin/users/{id}/enable
    // Habilita una cuenta de usuario deshabilitada
    // ---------------------------------------------------------
    @PatchMapping("/users/{id}/enable")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AdminResponse.UserDetail> enableUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        log.info("ADMIN '{}' habilitando usuario id: {}", currentUser.getUsername(), id);
        return ResponseEntity.ok(adminService.enableUser(id));
    }

    // ---------------------------------------------------------
    // DELETE /api/v1/admin/users/{id}
    // Elimina un usuario del sistema
    // ---------------------------------------------------------
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<AdminResponse.Message> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        log.info("ADMIN '{}' eliminando usuario id: {}", currentUser.getUsername(), id);
        adminService.deleteUser(id);
        return ResponseEntity.ok(AdminResponse.Message.builder()
                .message("Usuario eliminado correctamente")
                .build());
    }

    // ---------------------------------------------------------
    // GET /api/v1/admin/roles
    // Lista todos los roles del sistema
    // ---------------------------------------------------------
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<AdminResponse.RoleList> getAllRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }
}