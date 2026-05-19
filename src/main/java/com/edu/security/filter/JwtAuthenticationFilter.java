package com.edu.security.filter;


import com.edu.domain.entity.User;
import com.edu.repository.UserRepository;
import com.edu.security.util.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
// OncePerRequestFilter garantiza que este filtro se ejecuta exactamente
// una vez por request, evitando ejecuciones duplicadas en forwards/includes
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Rutas públicas — dejamos pasar sin validar token
        final String requestPath = request.getServletPath();
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", dejamos pasar
        // El SecurityConfig bloqueará el acceso si el endpoint requiere auth
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el token quitando el prefijo "Bearer "
        final String jwt = authHeader.substring(7);

        try {
            final String username = jwtService.extractUsername(jwt);

            // Solo procesamos si hay username y el contexto no tiene autenticación previa
            // Evita re-autenticar en el mismo request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByUsername(username)
                        .orElse(null);

                if (user != null && jwtService.isTokenValid(jwt, user)) {
                    // Construimos el objeto de autenticación con las authorities del usuario
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null, // credentials null porque ya autenticamos con JWT
                                    user.getAuthorities()
                            );

                    // Agregamos detalles del request (IP, session) al token de autenticación
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Guardamos la autenticación en el SecurityContext
                    // A partir de aquí Spring Security sabe quién es el usuario
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Usuario autenticado: {} | Path: {}", username, requestPath);
                }
            }
        } catch (Exception e) {
            // No propagamos la excepción — dejamos que el SecurityConfig
            // maneje el acceso denegado con el AuthenticationEntryPoint
            log.warn("No se pudo autenticar el token JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // Rutas que no requieren token — deben coincidir con las del SecurityConfig
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/");
    }
}