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

/**
 * Servicio principal para la gestión de Fondos de Inversión Colectiva (FIC).
 * <p>
 * Proporciona operaciones de consulta paginada, búsqueda por nombre y
 * sincronización de datos desde la API pública de datos.gov.co mediante
 * el protocolo SODA2.
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@Service
@RequiredArgsConstructor
public class FicService {

    /** Repositorio JPA para operaciones CRUD sobre la entidad FicData. */
    private final FicDataRepository ficRepo;

    /** Servicio de auditoría para registrar operaciones realizadas. */
    private final AuditService auditService;

    /** Constructor de WebClient para llamadas reactivas a APIs externas. */
    private final WebClient.Builder webClientBuilder;

    /**
     * Retorna una página de fondos de inversión almacenados en la base de datos.
     * <p>
     * Registra automáticamente la operación en el log de auditoría.
     * </p>
     *
     * @param pageable Objeto de paginación con número de página y tamaño
     * @return Página de objetos {@link FicData} con metadatos de paginación
     */
    public Page<FicData> listar(Pageable pageable) {
        auditService.registrar("FicData", "ALL", "READ",
            "localhost", "Listado de fondos");
        return ficRepo.findAll(pageable);
    }

    /**
     * Busca fondos cuyo nombre contenga el término especificado,
     * ignorando mayúsculas y minúsculas.
     *
     * @param nombre   Término de búsqueda parcial o completo
     * @param pageable Objeto de paginación con número de página y tamaño
     * @return Página de objetos {@link FicData} que coinciden con la búsqueda
     */
    public Page<FicData> buscarPorNombre(String nombre, Pageable pageable) {
        return ficRepo.findByNombreFondoContainingIgnoreCase(
            nombre, pageable);
    }

    /**
     * Sincroniza fondos de inversión desde la API pública de datos.gov.co
     * y los persiste en la base de datos local.
     * <p>
     * Utiliza el protocolo SODA2 para consultar el dataset {@code qhpu-8ixx}.
     * Los resultados se ordenan por fecha de corte descendente.
     * La operación queda registrada en el log de auditoría.
     * </p>
     *
     * @param limit  Número máximo de registros a obtener por llamada
     * @param offset Posición inicial para la paginación en la API externa
     * @return Número de registros sincronizados, o {@code -1} si ocurre un error
     */
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

            if (resultados == null || resultados.isEmpty()) return 0;

            for (Map<String, Object> fila : resultados) {
                FicData f = new FicData();
                f.setNombreFondo(
                    (String) fila.getOrDefault(
                        "nombre_patrimonio", "Sin nombre"));
                f.setSociedadGestora(
                    (String) fila.getOrDefault("nombre_entidad", ""));
                f.setTipoFondo(
                    (String) fila.getOrDefault(
                        "nombre_subtipo_patrimonio", ""));
                f.setPeriodo(
                    (String) fila.getOrDefault("fecha_corte", ""));
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
            System.err.println("Error al sincronizar: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Convierte un valor de tipo Object a Double de forma segura.
     * <p>
     * Maneja valores nulos y formatos de texto numérico provenientes
     * de la API externa.
     * </p>
     *
     * @param valor Objeto a convertir (puede ser String, Number o null)
     * @return Valor convertido a Double, o {@code null} si la conversión falla
     */
    private Double parseDouble(Object valor) {
        if (valor == null) return null;
        try {
            return Double.parseDouble(valor.toString());
        } catch (Exception e) {
            return null;
        }
    }
}