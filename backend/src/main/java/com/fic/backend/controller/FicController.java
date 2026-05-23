package com.fic.backend.controller;

import com.fic.backend.model.FicData;
import com.fic.backend.repository.FicDataRepository;
import com.fic.backend.service.AuditService;
import com.fic.backend.service.FicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fic")
@RequiredArgsConstructor
@Tag(name = "Fondos", description = "Consulta de fondos de inversion")
@CrossOrigin(origins = "*")
public class FicController {

    private final FicService ficService;
    private final FicDataRepository ficRepo;
    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Lista paginada de fondos")
    public ResponseEntity<Page<FicData>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String nombre) {

        PageRequest pageable = PageRequest.of(page, size);

        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(
                ficService.buscarPorNombre(nombre, pageable));
        }

        return ResponseEntity.ok(ficService.listar(pageable));
    }

    @GetMapping("/sync")
    @Operation(summary = "Sincroniza datos desde datos.gov.co")
    public ResponseEntity<String> sincronizar(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        int cantidad = ficService.sincronizarDesdeApi(limit, offset);
        return ResponseEntity.ok(
            "Sincronizados: " + cantidad + " registros");
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Estadisticas generales de los fondos")
    public ResponseEntity<Map<String, Object>> estadisticas() {

        List<FicData> todos = ficRepo.findAll();

        if (todos.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "totalFondos", 0,
                "mejorRentabilidad", 0,
                "promedioRentabilidad", 0,
                "totalInversionistas", 0,
                "nombreMejorFondo", "-"
            ));
        }

        // Total fondos
        long totalFondos = todos.size();

        // Mejor rentabilidad anual
        FicData mejorFondo = todos.stream()
            .filter(f -> f.getRentabilidad() != null)
            .max(Comparator.comparingDouble(FicData::getRentabilidad))
            .orElse(null);

        double mejorRent = mejorFondo != null
            ? mejorFondo.getRentabilidad() : 0;
        String nombreMejor = mejorFondo != null
            ? mejorFondo.getNombreFondo() : "-";

        // Promedio rentabilidad anual
        double promedio = todos.stream()
            .filter(f -> f.getRentabilidad() != null)
            .mapToDouble(FicData::getRentabilidad)
            .average()
            .orElse(0);

        // Total inversionistas
        long totalInv = todos.stream()
            .filter(f -> f.getNumInversionistas() != null)
            .mapToLong(FicData::getNumInversionistas)
            .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFondos", totalFondos);
        stats.put("mejorRentabilidad",
            Math.round(mejorRent * 100.0) / 100.0);
        stats.put("promedioRentabilidad",
            Math.round(promedio * 100.0) / 100.0);
        stats.put("totalInversionistas", totalInv);
        stats.put("nombreMejorFondo", nombreMejor);

        auditService.registrar("FicData", "STATS", "READ",
            "localhost", "Consulta de estadisticas");

        return ResponseEntity.ok(stats);
    }
}