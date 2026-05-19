package com.edu.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "revoked", "expiresAt"})
@EqualsAndHashCode(of = "id")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El token en sí — UUID o string aleatorio largo
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    // Relación con el usuario dueño del token
    // LAZY porque no siempre necesitamos cargar el User completo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // false = activo, true = revocado (logout o rotación)
    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Método de utilidad para verificar si el token sigue siendo válido
    public boolean isValid() {
        return !this.revoked && this.expiresAt.isAfter(LocalDateTime.now());
    }
}