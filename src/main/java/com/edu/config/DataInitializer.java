package com.edu.config;

import com.edu.domain.entity.Role;
import com.edu.domain.entity.User;
import com.edu.repository.RoleRepository;
import com.edu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
// ApplicationRunner se ejecuta una vez después de que el contexto
// de Spring está completamente cargado — ideal para inicialización de datos
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Credenciales del admin inicial — vienen del application.yml
    // En producción se sobreescriben con variables de entorno
    @Value("${application.admin.username}")
    private String adminUsername;

    @Value("${application.admin.email}")
    private String adminEmail;

    @Value("${application.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // Verificar si ya existe algún usuario con rol ADMIN
        // Si existe, no hacemos nada — evita duplicados en cada reinicio
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException(
                        "Rol ROLE_ADMIN no encontrado — verifica las migraciones Flyway"
                ));

        boolean adminExists = userRepository.existsByUsername(adminUsername);

        if (adminExists) {
            log.info("Admin inicial ya existe — omitiendo inicialización");
            return;
        }

        // Crear el primer admin del sistema
        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);

        log.info("====================================================");
        log.info("Admin inicial creado correctamente");
        log.info("Username : {}", adminUsername);
        log.info("Email    : {}", adminEmail);
        log.info("IMPORTANTE: Cambia la contraseña después del primer login");
        log.info("====================================================");
    }
}