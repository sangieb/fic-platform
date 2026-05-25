package com.fic.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Fondo de Inversión Colectiva (FIC) de Colombia.
 * <p>
 * Los datos son obtenidos desde la API pública de datos.gov.co y almacenados
 * en la tabla {@code fic_data} de la base de datos PostgreSQL.
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@Entity
@Table(name = "fic_data")
@Data
@NoArgsConstructor
public class FicData {

    /**
     * Identificador único autogenerado del fondo.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre oficial del patrimonio o fondo de inversión.
     * Corresponde al campo {@code nombre_patrimonio} de la API.
     */
    @Column(name = "nombre_fondo")
    private String nombreFondo;

    /**
     * Nombre de la entidad gestora o administradora del fondo.
     * Corresponde al campo {@code nombre_entidad} de la API.
     */
    @Column(name = "sociedad_gestora")
    private String sociedadGestora;

    /**
     * Rentabilidad anual del fondo expresada en porcentaje.
     * Corresponde al campo {@code rentabilidad_anual} de la API.
     */
    @Column(name = "rentabilidad")
    private Double rentabilidad;

    /**
     * Rentabilidad diaria del fondo expresada en porcentaje.
     * Corresponde al campo {@code rentabilidad_diaria} de la API.
     */
    @Column(name = "rentabilidad_diaria")
    private Double rentabilidadDiaria;

    /**
     * Rentabilidad mensual del fondo expresada en porcentaje.
     * Corresponde al campo {@code rentabilidad_mensual} de la API.
     */
    @Column(name = "rentabilidad_mensual")
    private Double rentabilidadMensual;

    /**
     * Rentabilidad semestral del fondo expresada en porcentaje.
     * Corresponde al campo {@code rentabilidad_semestral} de la API.
     */
    @Column(name = "rentabilidad_semestral")
    private Double rentabilidadSemestral;

    /**
     * Fecha de corte del reporte en formato ISO.
     * Corresponde al campo {@code fecha_corte} de la API.
     */
    @Column(name = "periodo")
    private String periodo;

    /**
     * Subtipo o clasificación del fondo.
     * Corresponde al campo {@code nombre_subtipo_patrimonio} de la API.
     */
    @Column(name = "tipo_fondo")
    private String tipoFondo;

    /**
     * Valor total del fondo al cierre del día expresado en pesos colombianos.
     * Corresponde al campo {@code valor_fondo_cierre_dia_t} de la API.
     */
    @Column(name = "valor_fondo")
    private Double valorFondo;

    /**
     * Número total de inversionistas activos en el fondo.
     * Corresponde al campo {@code numero_inversionistas} de la API.
     */
    @Column(name = "num_inversionistas")
    private Integer numInversionistas;

    /**
     * Fuente de origen de los datos. Valor fijo: {@code "datos.gov.co"}.
     */
    private String fuente;

    /**
     * Fecha y hora en que el registro fue creado en la base de datos local.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}