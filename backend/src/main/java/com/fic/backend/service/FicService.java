package com.fic.backend.service;

import com.fic.backend.model.FicData;
import com.fic.backend.repository.FicDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FicService {

    private final FicDataRepository ficRepo;
    private final AuditService auditService;
    private final WebClient.Builder webClientBuilder;

    public Page<FicData> listar(Pageable pageable) {
        auditService.registrar("FicData", "ALL", "READ",
            "localhost", "Listado de fondos");
        return ficRepo.findAll(pageable);
    }

    public Page<FicData> buscarPorNombre(String nombre, Pageable pageable) {
        return ficRepo.findByNombreFondoContainingIgnoreCase(
            nombre, pageable);
    }

    public int sincronizarDesdeApi(int limit, int offset) {
        try {
            List<Map> resultados = webClientBuilder
                .baseUrl("https://www.datos.gov.co")
                .build()
                .get()
                .uri(u -> u
                    .path("/resource/qhpu-8ixx.json")
                    .queryParam("$limit", limit)
                    .queryParam("$offset", offset)
                    .queryParam("$order", "fecha_corte DESC")
                    .build())
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();

            if (resultados == null || resultados.isEmpty()) {
                return 0;
            }

            // Muestra los campos reales de la API en consola
            System.out.println("=== CAMPOS DE LA API ===");
            resultados.get(0).forEach((k, v) ->
                System.out.println(k + " = " + v));
            System.out.println("========================");

            for (Map<String, Object> fila : resultados) {
                FicData f = new FicData();

                f.setNombreFondo(
                    (String) fila.getOrDefault("nombre_patrimonio", "Sin nombre"));
                f.setSociedadGestora(
                    (String) fila.getOrDefault("nombre_entidad", ""));
                f.setTipoFondo(
                    (String) fila.getOrDefault("nombre_subtipo_patrimonio", ""));
                f.setPeriodo(
                    (String) fila.getOrDefault("fecha_corte", ""));

                // Rentabilidades — vienen como String, hay que convertirlas
                f.setRentabilidad(
                    parseDouble(fila.get("rentabilidad_anual")));
                f.setRentabilidadDiaria(
                    parseDouble(fila.get("rentabilidad_diaria")));
                f.setRentabilidadMensual(
                    parseDouble(fila.get("rentabilidad_mensual")));
                f.setRentabilidadSemestral(
                    parseDouble(fila.get("rentabilidad_semestral")));
                f.setValorFondo(
                    parseDouble(fila.get("valor_fondo_cierre_dia_t")));

                // Inversionistas
                Object inv = fila.get("numero_inversionistas");
                if (inv != null) {
                    try {
                        f.setNumInversionistas(
                            Integer.parseInt(inv.toString()));
                    } catch (Exception ignored) {}
                }

                f.setFuente("datos.gov.co");
                f.setCreatedAt(LocalDateTime.now());
                ficRepo.save(f);
            }

            auditService.registrar(
                "FicData",
                String.valueOf(resultados.size()),
                "CREATE", "sistema",
                "Sincronizacion desde datos.gov.co"
            );

            return resultados.size();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return -1;
        }
    }
    
    private Double parseDouble(Object valor) {
        if (valor == null) return null;
        try {
            return Double.parseDouble(valor.toString());
        } catch (Exception e) {
            return null;
        }
    }
}