package com.edu.service.impl.worker;

import com.edu.domain.dto.request.worker.WorkerRequest;
import com.edu.domain.dto.response.worker.WorkerResponse;
import com.edu.domain.entity.Role;
import com.edu.domain.entity.User;
import com.edu.repository.UserRepository;
import com.edu.service.worker.WorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerServiceImpl implements WorkerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public WorkerResponse.Profile getProfile(String username) {
        return toProfile(findUserOrThrow(username));
    }

    @Override
    @Transactional
    public WorkerResponse.Profile updateProfile(String username, WorkerRequest.UpdateProfile request) {
        User user = findUserOrThrow(username);

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (!request.getUsername().equals(username)) {
                if (userRepository.existsByUsername(request.getUsername())) {
                    throw new IllegalArgumentException("El username ya está en uso");
                }
                user.setUsername(request.getUsername());
            }
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new IllegalArgumentException("El email ya está registrado");
                }
                user.setEmail(request.getEmail());
            }
        }

        User saved = userRepository.save(user);
        log.info("Perfil actualizado para usuario: {}", saved.getUsername());
        return toProfile(saved);
    }

    @Override
    @Transactional
    public void changePassword(String username, WorkerRequest.ChangePassword request) {
        User user = findUserOrThrow(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("La nueva contraseña y la confirmación no coinciden");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Contraseña actualizada para usuario: {}", username);
    }

    // ---------------------------------------------------------
    // Métodos privados
    // ---------------------------------------------------------
    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado: " + username
                ));
    }

    private WorkerResponse.Profile toProfile(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        return WorkerResponse.Profile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .build();
    }
}