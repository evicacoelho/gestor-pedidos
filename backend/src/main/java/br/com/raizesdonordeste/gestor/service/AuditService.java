package br.com.raizesdonordeste.gestor.service;

import br.com.raizesdonordeste.gestor.domain.entity.AuditLog;
import br.com.raizesdonordeste.gestor.domain.entity.Order;
import br.com.raizesdonordeste.gestor.domain.enums.AuditAction;
import br.com.raizesdonordeste.gestor.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(Order order, AuditAction action, String reason, String performedBy) {
        AuditLog log = new AuditLog();
        log.setOrder(order);
        log.setAction(action);
        log.setReason(reason);
        log.setPerformedBy(performedBy);
        auditLogRepository.save(log);
    }
}
