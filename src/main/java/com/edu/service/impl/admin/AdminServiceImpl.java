package com.edu.service.impl.admin;

import com.edu.domain.dto.response.admin.AdminResponse;
import com.edu.domain.entity.Role;
import com.edu.domain.entity.User;
import com.edu.repository.RoleRepository;
import com.edu.repository.UserRepository;
import com.edu.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.edu.domain.dto.request.admin.AdminRequest;

import org.springframework.security.crypto.password.PasswordEncoder;



@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------------------------------------------------------
    // Crear usuario con rol seleccionable (ADMIN o TRABAJADOR)
    // ---------------------------------------------------------
    @Override
    @Transactional
    public AdminResponse.UserDetail createUser(AdminRequest.CreateUser request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El username ya está en uso");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Busca el rol según lo que eligió el ADMIN en el select
        Role role = roleRepository.findByName(request.getRole().name())
                .orElseThrow(() -> new IllegalStateException(
                        "Rol " + request.getRole().name() + " no encontrado"
                ));

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Set.of(role))
                .build();

        User saved = userRepository.save(newUser);
        log.info("Usuario '{}' creado con rol '{}'", saved.getUsername(), request.getRole().name());

        return toUserDetail(saved);
    }

    // ---------------------------------------------------------
    // Listar todos los usuarios
    // ---------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public AdminResponse.UserList getAllUsers() {
        List<User> users = userRepository.findAll();

        List<AdminResponse.UserDetail> details = users.stream()
                .map(this::toUserDetail)
                .collect(Collectors.toList());

        return AdminResponse.UserList.builder()
                .users(details)
                .total(details.size())
                .build();
    }

    // ---------------------------------------------------------
    // Obtener usuario por ID
    // ---------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public AdminResponse.UserDetail getUserById(Long id) {
        return toUserDetail(findUserOrThrow(id));
    }

    // ---------------------------------------------------------
    // Promover usuario existente a ADMIN
    // ---------------------------------------------------------
    @Override
    @Transactional
    public AdminResponse.UserDetail promoteToAdmin(Long id) {
        User user = findUserOrThrow(id);

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("Rol ROLE_ADMIN no encontrado"));

        if (user.getRoles().contains(adminRole)) {
            throw new IllegalArgumentException("El usuario ya tiene el rol ADMIN");
        }

        user.getRoles().add(adminRole);
        User saved = userRepository.save(user);

        log.info("Usuario '{}' promovido a ADMIN", user.getUsername());
        return toUserDetail(saved);
    }

    // ---------------------------------------------------------
    // Deshabilitar usuario
    // ---------------------------------------------------------
    @Override
    @Transactional
    public AdminResponse.UserDetail disableUser(Long id) {
        User user = findUserOrThrow(id);

        if (!user.getEnabled()) {
            throw new IllegalArgumentException("El usuario ya está deshabilitado");
        }

        user.setEnabled(false);
        User saved = userRepository.save(user);

        log.info("Usuario '{}' deshabilitado", user.getUsername());
        return toUserDetail(saved);
    }

    // ---------------------------------------------------------
    // Habilitar usuario
    // ---------------------------------------------------------
    @Override
    @Transactional
    public AdminResponse.UserDetail enableUser(Long id) {
        User user = findUserOrThrow(id);

        if (user.getEnabled()) {
            throw new IllegalArgumentException("El usuario ya está habilitado");
        }

        user.setEnabled(true);
        User saved = userRepository.save(user);

        log.info("Usuario '{}' habilitado", user.getUsername());
        return toUserDetail(saved);
    }

    // ---------------------------------------------------------
    // Eliminar usuario
    // ---------------------------------------------------------
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
        log.info("Usuario '{}' eliminado", user.getUsername());
    }

    // ---------------------------------------------------------
    // Listar todos los roles
    // ---------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public AdminResponse.RoleList getAllRoles() {
        List<Role> roles = roleRepository.findAll();

        List<AdminResponse.RoleDetail> details = roles.stream()
                .map(this::toRoleDetail)
                .collect(Collectors.toList());

        return AdminResponse.RoleList.builder()
                .roles(details)
                .total(details.size())
                .build();
    }

    // ---------------------------------------------------------
    // Métodos privados
    // ---------------------------------------------------------
    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario con id " + id + " no encontrado"
                ));
    }

    private AdminResponse.UserDetail toUserDetail(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        return AdminResponse.UserDetail.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .roles(roles)
                .permissions(permissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private AdminResponse.RoleDetail toRoleDetail(Role role) {
        Set<String> permissions = role.getPermissions().stream()
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        return AdminResponse.RoleDetail.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissions)
                .build();
    }
}