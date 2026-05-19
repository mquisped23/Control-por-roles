package com.edu.config;



import com.edu.security.filter.JwtAuthenticationFilter;
import com.edu.security.handler.AuthEntryPoint;
import com.edu.security.handler.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
// Habilita @PreAuthorize, @PostAuthorize y @Secured a nivel de método
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final AuthEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    // =========================================================
    // FILTER CHAIN — reglas de seguridad HTTP
    // =========================================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF deshabilitado: API stateless con JWT no necesita protección CSRF
                // React enviará tokens en el header, no en cookies de sesión
                .csrf(AbstractHttpConfigurer::disable)

                // CORS configurado para permitir React en desarrollo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Manejo de errores de autenticación y autorización
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // STATELESS: sin sesiones HTTP — cada request se autentica con JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Reglas de autorización por endpoint
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos — no requieren token
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Actuator health — accesible sin autenticación
                        .requestMatchers("/actuator/health").permitAll()
                        // Endpoints exclusivos de ADMIN
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Endpoints de trabajador — ADMIN también puede acceder
                        .requestMatchers("/api/v1/worker/**").hasAnyRole("ADMIN", "TRABAJADOR")
                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                )

                // Registramos nuestro proveedor de autenticación
                .authenticationProvider(authenticationProvider())

                // Nuestro filtro JWT se ejecuta ANTES del filtro estándar de username/password
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =========================================================
    // CORS — permite requests desde React en desarrollo
    // En producción reemplaza localhost:5173 por tu dominio real
    // =========================================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos — React dev server por defecto usa puerto 5173 (Vite)
        // o 3000 (Create React App)
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers permitidos en el request
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Headers expuestos al cliente en la respuesta
        config.setExposedHeaders(List.of("Authorization"));

        // Permite enviar cookies y headers de autenticación en requests cross-origin
        config.setAllowCredentials(true);

        // Tiempo que el navegador puede cachear la respuesta del preflight OPTIONS
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // =========================================================
    // AUTHENTICATION PROVIDER
    // Conecta UserDetailsService + PasswordEncoder con Spring Security
    // =========================================================
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // =========================================================
    // AUTHENTICATION MANAGER
    // Necesario para el AuthService cuando procesamos el login
    // =========================================================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    // =========================================================
    // PASSWORD ENCODER — BCrypt con strength 12
    // Strength 10 es el defecto, 12 es más seguro con costo aceptable
    // =========================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}