package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.contract.ContractRequestDto;
import com.microshop.rrhh.application.dto.contract.ContractResponseDto;
import com.microshop.rrhh.application.mapper.ContractMapper;
import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Contract;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.infrastructure.persistence.repository.ContractRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class ContractCommandService {

    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractMapper contractMapper;
    private final TenantContext tenantContext;
    private final MessageHelper msg;

    public ContractResponseDto createContract(@Valid ContractRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(request.employeeId(), tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("contract.employee.not.found")));

        // Finalizar contrato activo anterior si existe
        contractRepository.findActiveByEmployeeId(tenantId, request.employeeId())
                .ifPresent(existing -> {
                    existing.setEstado(Contract.ContractStatus.FINALIZADO);
                    existing.setMotivoFin("Reemplazado por nuevo contrato");
                    contractRepository.save(existing);
                    log.info("Contrato anterior {} finalizado automáticamente", existing.getId());
                });

        Contract contract = contractMapper.toEntity(request, tenantId);
        contract.setEmployee(employee);

        Contract saved = contractRepository.save(contract);
        log.info("Contrato creado: {} para empleado {} - Tenant: {}", saved.getId(), request.employeeId(), tenantId);
        return contractMapper.toDto(saved);
    }

    public ContractResponseDto updateContract(Long id, @Valid ContractRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Contract contract = contractRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("contract.not.found")));

        if (contract.getEstado() == Contract.ContractStatus.FINALIZADO) {
            throw new BusinessException(msg.get("contract.estado.invalido", contract.getEstado().name()));
        }

        contractMapper.updateEntity(contract, request);
        Contract updated = contractRepository.save(contract);
        log.info("Contrato actualizado: {} - Tenant: {}", updated.getId(), tenantId);
        return contractMapper.toDto(updated);
    }

    public ContractResponseDto terminateContract(Long id, String motivoFin) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Contract contract = contractRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("contract.not.found")));

        if (contract.getEstado() != Contract.ContractStatus.ACTIVO) {
            throw new BusinessException(msg.get("contract.estado.invalido", contract.getEstado().name()));
        }

        contract.setEstado(Contract.ContractStatus.FINALIZADO);
        contract.setMotivoFin(motivoFin);
        Contract updated = contractRepository.save(contract);
        log.info("Contrato terminado: {} - Tenant: {}", id, tenantId);
        return contractMapper.toDto(updated);
    }

    public ContractResponseDto renewContract(Long id, @Valid ContractRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Contract oldContract = contractRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("contract.not.found")));

        // Marcar el viejo como RENOVADO
        oldContract.setEstado(Contract.ContractStatus.RENOVADO);
        oldContract.setMotivoFin("Renovado");
        contractRepository.save(oldContract);

        // Crear nuevo contrato
        Contract newContract = contractMapper.toEntity(request, tenantId);
        newContract.setEmployee(oldContract.getEmployee());
        Contract saved = contractRepository.save(newContract);

        log.info("Contrato {} renovado con nuevo contrato {} - Tenant: {}", id, saved.getId(), tenantId);
        return contractMapper.toDto(saved);
    }
}
