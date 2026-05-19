package com.edu.security.util;


import com.edu.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // =========================================================
    // GENERACIÓN DE TOKENS
    // =========================================================

    public String generateAccessToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Incluimos roles y permisos como claims dentro del token
        // Así el receptor puede verificar autorización sin consultar la BD
        extraClaims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .toList());
        extraClaims.put("email", user.getEmail());

        return buildToken(extraClaims, user, accessTokenExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                // iss (issuer) identifica quién emitió el token
                .issuer("security-roles-app")
                // iat (issued at) y exp (expiration) son claims estándar JWT
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // =========================================================
    // VALIDACIÓN DE TOKENS
    // =========================================================

    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUsername(token);
            return username.equals(user.getUsername()) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado para el usuario: {}", user.getUsername());
            return false;
        } catch (SignatureException e) {
            log.warn("Firma JWT inválida");
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformado");
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT no soportado");
            return false;
        }
    }

    // =========================================================
    // EXTRACCIÓN DE CLAIMS
    // =========================================================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =========================================================
    // CLAVE DE FIRMA
    // =========================================================

    private SecretKey getSigningKey() {
        // HMAC-SHA256 requiere mínimo 256 bits (32 bytes)
        // La clave viene de application.yml — en prod desde variable de entorno
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}