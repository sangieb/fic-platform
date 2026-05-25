package com.fic.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que registra todas las operaciones realizadas sobre las entidades
 * del sistema (CREATE, READ, UPDATE, DELETE).
 * <p>
 * Almacenada en la tabla {@code audit_log} de PostgreSQL. Cada acción
 * relevante del sistema genera automáticamente un registro de auditoría.
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
public class AuditLog {

    /** Identificador único autogenerado del registro de auditoría. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la entidad afectada. Ejemplo: {@code "FicData"}, {@code "Reporte"}. */
    @Column(name = "entity_name")
    private String entityName;

    /** Clave primaria de la entidad afectada. */
    @Column(name = "entity_pk")
    private String entityPk;

    /**
     * Tipo de acción realizada.
     * Valores posibles: {@code CREATE}, {@code READ}, {@code UPDATE}, {@code DELETE}.
     */
    private String action;

    /** Usuario o proceso que realizó la acción. */
    @Column(name = "changed_by")
    private String changedBy;

    /** Fecha y hora exacta en que ocurrió la acción. */
    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    /** Valor anterior de la entidad antes de la modificación (formato JSON). */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /** Valor nuevo de la entidad después de la modificación (formato JSON). */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /** Dirección IP desde donde se originó la acción. */
    @Column(name = "ip_address")
    private String ipAddress;

    /** Descripción legible de la acción realizada. */
    private String description;
}