package com.fic.backend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utilidad para la generación y validación de tokens JWT (JSON Web Token).
 * <p>
 * Proporciona métodos para crear tokens de acceso administrativo y
 * validar su autenticidad y vigencia. Utilizado exclusivamente para
 * proteger el endpoint {@code /api/audit}.
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@Component
public class JwtUtil {

    /** Clave secreta para firmar los tokens JWT, leída desde application.properties. */
    @Value("${jwt.secret:ficColombiaSecretKey2026XyZ987654321AB}")
    private String secret;

    /** Duración del token en milisegundos (24 horas por defecto). */
    private static final long EXPIRACION_MS = 24 * 60 * 60 * 1000L;

    /**
     * Genera la clave de firma HMAC-SHA256 a partir del secreto configurado.
     *
     * @return Clave criptográfica para firmar y verificar tokens
     */
    private Key getClave() {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un token JWT para el usuario administrador.
     * <p>
     * El token incluye el nombre de usuario como sujeto, la fecha de
     * emisión y la fecha de expiración (24 horas desde la emisión).
     * </p>
     *
     * @param username Nombre de usuario para incluir en el token
     * @return Token JWT firmado en formato compacto (Header.Payload.Signature)
     */
    public String generarToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
            .signWith(getClave(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Valida un token JWT verificando su firma y fecha de expiración.
     *
     * @param token Token JWT a validar
     * @return {@code true} si el token es válido y no ha expirado,
     *         {@code false} en caso contrario
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getClave())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extrae el nombre de usuario (subject) de un token JWT válido.
     *
     * @param token Token JWT del que extraer el usuario
     * @return Nombre de usuario contenido en el token
     */
    public String obtenerUsuario(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getClave())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}