package com.fic.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad de Spring Security.
 * <p>
 * Define las reglas de acceso para los endpoints de la aplicación.
 * Solo el endpoint {@code /api/audit} requiere autenticación.
 * El endpoint {@code /api/auth/login} es público para obtener el JWT.
 * Todo lo demás es accesible sin autenticación (plataforma pública).
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Clave de acceso administrativo leída desde application.properties. */
    @Value("${admin.audit-key}")
    private String auditKey;

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     * <p>
     * Deshabilita CSRF, permite acceso público a todos los endpoints
     * excepto {@code /api/audit/**} que requiere autenticación básica.
     * </p>
     *
     * @param http Objeto de configuración de seguridad HTTP
     * @return Cadena de filtros de seguridad configurada
     * @throws Exception si ocurre un error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/audit/**").authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Configura el servicio de usuarios en memoria para autenticación básica.
     * <p>
     * Define un único usuario administrador cuya contraseña corresponde
     * a la clave configurada en {@code application.properties}.
     * </p>
     *
     * @return Servicio de usuarios con el administrador configurado
     */
    @Bean
    public UserDetailsService users() {
        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin")
            .password(auditKey)
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }
}