package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.employee.*;
import com.microshop.rrhh.application.mapper.EmployeeSubResourceMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.*;
import com.microshop.rrhh.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmployeeSubResourceQueryService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final DependentRepository dependentRepository;
    private final DocumentRepository documentRepository;
    private final SalaryRepository salaryRepository;
    private final TenantContext tenantContext;
    private final EmployeeSubResourceMapper mapper;

    // ── Emergency Contacts ────────────────────────────────────────────────────

    public List<EmergencyContactDto.Response> getEmergencyContacts(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return emergencyContactRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(mapper::toEmergencyContactDto)
                .toList();
    }

    // ── Dependents ────────────────────────────────────────────────────────────

    public List<DependentDto.Response> getDependents(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return dependentRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(mapper::toDependentDto)
                .toList();
    }

    // ── Documents ─────────────────────────────────────────────────────────────

    public List<DocumentDto.Response> getDocuments(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return documentRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(mapper::toDocumentDto)
                .toList();
    }

    // ── Salary History ────────────────────────────────────────────────────────

    public List<SalaryDto.Response> getSalaryHistory(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return salaryRepository.findByTenantIdAndEmployeeIdOrderByFechaInicioDesc(tenantId, employeeId).stream()
                .map(mapper::toSalaryDto)
                .toList();
    }
}
