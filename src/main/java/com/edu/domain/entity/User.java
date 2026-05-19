package com.edu.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "username", "email"})
@EqualsAndHashCode(of = "id")
// User implementa UserDetails para integrarse directamente con Spring Security
// Spring Security usa esta interfaz para autenticar y autorizar
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // La contraseña siempre se almacena hasheada con BCrypt — nunca en texto plano
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // EAGER porque Spring Security necesita los roles en cada request
    // para construir el SecurityContext
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // =========================================================
    // UserDetails — Spring Security usa estos métodos
    // =========================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Construimos las authorities desde roles Y permisos
        // Roles:       "ROLE_ADMIN", "ROLE_TRABAJADOR"
        // Permisos:    "user:read", "user:write", etc.
        // Spring Security distingue roles de permisos por el prefijo ROLE_
        Set<GrantedAuthority> authorities = new HashSet<>();

        roles.forEach(role -> {
            // Agregar el rol como authority
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            // Agregar cada permiso del rol como authority individual
            role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getName()))
            );
        });

        return authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    // Cuenta no expirada — se puede extender si necesitas expiración de cuentas
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Cuenta no bloqueada — se puede extender para bloquear por intentos fallidos
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Credenciales no expiradas — se puede extender para forzar cambio de password
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}