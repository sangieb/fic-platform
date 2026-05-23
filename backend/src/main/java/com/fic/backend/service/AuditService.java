package com.fic.backend.service;

import com.fic.backend.model.AuditLog;
import com.fic.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditRepo;

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