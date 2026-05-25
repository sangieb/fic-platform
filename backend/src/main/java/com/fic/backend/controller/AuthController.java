package com.fic.backend.controller;

import com.fic.backend.config.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para autenticación administrativa mediante JWT.
 * <p>
 * Expone el endpoint {@code /api/auth/login} que permite obtener un
 * token JWT válido para acceder al endpoint protegido {@code /api/audit}.
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login administrativo con JWT")
@CrossOrigin(origins = "*")
public class AuthController {

    /** Utilidad para generación y validación de tokens JWT. */
    private final JwtUtil jwtUtil;

    /** Clave de administrador leída desde application.properties. */
    @Value("${admin.audit-key}")
    private String auditKey;

    /**
     * Autentica al administrador y genera un token JWT.
     * <p>
     * Recibe usuario y contraseña, verifica las credenciales contra
     * la clave configurada y retorna un token JWT válido por 24 horas.
     * </p>
     *
     * @param credenciales Mapa con campos {@code usuario} y {@code clave}
     * @return Token JWT si las credenciales son correctas,
     *         o error 401 si son incorrectas
     */
    @PostMapping("/login")
    @Operation(summary = "Obtiene token JWT para acceso administrativo")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> credenciales) {

        String usuario = credenciales.get("usuario");
        String clave   = credenciales.get("clave");

        if ("admin".equals(usuario) && auditKey.equals(clave)) {
            String token = jwtUtil.generarToken(usuario);
            return ResponseEntity.ok(Map.of(
                "token", token,
                "tipo", "Bearer",
                "expira", "24 horas",
                "usuario", usuario
            ));
        }

        return ResponseEntity.status(401).body(
            Map.of("error", "Credenciales incorrectas"));
    }
}