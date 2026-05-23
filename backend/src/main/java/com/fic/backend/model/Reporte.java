package com.fic.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reporte")
@Data
@NoArgsConstructor
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String formato;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "filtros_json", columnDefinition = "TEXT")
    private String filtrosJson;
}