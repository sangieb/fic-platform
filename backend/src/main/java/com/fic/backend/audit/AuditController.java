package com.fic.backend.audit;

import com.fic.backend.model.AuditLog;
import com.fic.backend.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Logs del sistema - requiere autenticación")
public class AuditController {

    private final AuditLogRepository auditRepo;

    @GetMapping
    @Operation(summary = "Lista todos los logs de auditoría")
    public ResponseEntity<Page<AuditLog>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(
            auditRepo.findAll(PageRequest.of(page, size)));
    }
}