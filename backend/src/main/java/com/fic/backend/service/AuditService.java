package com.fic.backend.service;

import com.fic.backend.model.AuditLog;
import com.fic.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Servicio encargado de registrar todas las operaciones de auditoría
 * del sistema en la base de datos.
 * <p>
 * Es utilizado por los demás servicios y controladores para dejar
 * trazabilidad de cada acción relevante realizada sobre las entidades.
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    /** Repositorio JPA para persistir los registros de auditoría. */
    private final AuditLogRepository auditRepo;

    /**
     * Registra una acción de auditoría en la base de datos.
     * <p>
     * Este método es llamado automáticamente desde los servicios y
     * controladores cada vez que ocurre una operación relevante.
     * </p>
     *
     * @param entidad     Nombre de la entidad afectada (ej: {@code "FicData"})
     * @param pk          Clave primaria o identificador de la entidad afectada
     * @param accion      Tipo de acción: {@code CREATE}, {@code READ},
     *                    {@code UPDATE} o {@code DELETE}
     * @param ip          Dirección IP desde donde se originó la acción
     * @param descripcion Descripción legible de la operación realizada
     */
    public void registrar(String entidad, String pk, String accion,
                          String ip, String descripcion) {
        AuditLog log = new AuditLog();
        log.setEntityName(entidad);
        log.setEntityPk(pk);
        log.setAction(accion);
        log.setChangedBy("sistema");
        log.setChangedAt(LocalDateTime.now());
        log.setIpAddress(ip);
        log.setDescription(descripcion);
        auditRepo.save(log);
    }
}