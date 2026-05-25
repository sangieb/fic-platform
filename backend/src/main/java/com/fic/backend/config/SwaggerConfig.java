package com.fic.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para la documentación de la API.
 * <p>
 * Expone la documentación interactiva en {@code /swagger-ui.html} y
 * configura la autenticación Basic Auth para el endpoint protegido
 * {@code /api/audit}.
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configura la documentación OpenAPI con información del proyecto
     * y esquema de seguridad Basic Auth.
     *
     * @return Configuración OpenAPI completa
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("FIC Colombia API")
                .version("1.0")
                .description(
                    "API publica para consultar Fondos de Inversion "
                    + "Colectiva de Colombia. "
                    + "El endpoint /api/audit requiere autenticacion Basic Auth. "
                    + "Usuario: admin / Clave: claveAdmin2024"))
            .addSecurityItem(
                new SecurityRequirement().addList("basicAuth"))
            .components(new Components()
                .addSecuritySchemes("basicAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")
                        .description(
                            "Autenticacion basica para /api/audit. "
                            + "Usuario: admin")));
    }
}