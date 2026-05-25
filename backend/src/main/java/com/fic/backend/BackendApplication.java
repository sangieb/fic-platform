package com.fic.backend;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;

/**
 * Clase principal de la aplicación FIC Colombia Backend.
 * <p>
 * Configura Spring Boot con soporte HTTPS mediante certificado SSL
 * y redirige automáticamente el tráfico HTTP al puerto HTTPS seguro.
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@SpringBootApplication
public class BackendApplication {

    /** Puerto HTTP para redirección a HTTPS. */
    @Value("${server.http.port:8080}")
    private int httpPort;

    /** Puerto HTTPS principal. */
    @Value("${server.port:8443}")
    private int httpsPort;

    /**
     * Punto de entrada principal de la aplicación.
     *
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Configura un conector HTTP adicional que redirige
     * automáticamente todas las peticiones al puerto HTTPS.
     * <p>
     * Esto permite que las peticiones a {@code http://localhost:8080}
     * sean redirigidas automáticamente a {@code https://localhost:8443}.
     * </p>
     *
     * @return Fábrica de servidor web con conector HTTP configurado
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat =
            new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(crearConectorHTTP());
        return tomcat;
    }

    /**
     * Crea un conector HTTP que redirige al puerto HTTPS.
     *
     * @return Conector Tomcat configurado para redirección HTTPS
     */
    private Connector crearConectorHTTP() {
        Connector connector = new Connector(
            TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(httpsPort);
        return connector;
    }
}