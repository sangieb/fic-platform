package com.fic.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que registra cada reporte generado por el sistema.
 * <p>
 * Almacenada en la tabla {@code reporte} de PostgreSQL. Cada descarga
 * de PDF o Excel genera un registro en esta tabla para trazabilidad.
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(name = "reporte")
@Data
@NoArgsConstructor
public class Reporte {

    /** Identificador único autogenerado del reporte. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Título del reporte generado, traducido según el idioma activo. */
    private String titulo;

    /**
     * Formato del reporte generado.
     * Valores posibles: {@code "PDF"}, {@code "EXCEL"}.
     */
    private String formato;

    /** Fecha y hora exacta en que se generó el reporte. */
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    /** Filtros aplicados al generar el reporte, en formato JSON. */
    @Column(name = "filtros_json", columnDefinition = "TEXT")
    private String filtrosJson;
}