package com.fic.backend.controller;

import com.fic.backend.model.FicData;
import com.fic.backend.service.FicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fic")
@RequiredArgsConstructor
@Tag(name = "Fondos", description = "Consulta de fondos de inversión")
@CrossOrigin(origins = "*")
public class FicController {

    private final FicService ficService;

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
}