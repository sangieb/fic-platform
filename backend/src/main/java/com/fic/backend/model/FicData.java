package com.fic.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "fic_data")
@Data
@NoArgsConstructor
public class FicData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_fondo")
    private String nombreFondo;

    @Column(name = "sociedad_gestora")
    private String sociedadGestora;

    @Column(name = "rentabilidad")
    private Double rentabilidad;

    @Column(name = "rentabilidad_diaria")
    private Double rentabilidadDiaria;

    @Column(name = "rentabilidad_mensual")
    private Double rentabilidadMensual;

    @Column(name = "rentabilidad_semestral")
    private Double rentabilidadSemestral;

    @Column(name = "periodo")
    private String periodo;

    @Column(name = "tipo_fondo")
    private String tipoFondo;

    @Column(name = "valor_fondo")
    private Double valorFondo;

    @Column(name = "num_inversionistas")
    private Integer numInversionistas;

    private String fuente;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}