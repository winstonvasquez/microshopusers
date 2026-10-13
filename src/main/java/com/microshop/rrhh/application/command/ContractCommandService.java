package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.contract.ContractRequestDto;
import com.microshop.rrhh.application.dto.contract.ContractResponseDto;
import com.microshop.rrhh.application.dto.employee.SalaryDto;
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
    private final EmployeeSubResourceCommandService employeeSubResourceCommandService;

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
        reactivarSiCesado(employee);
        sincronizarSalario(employee, saved);
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
        // La planilla del mes del cese necesita la fecha de fin real del contrato.
        // Un plazo fijo terminado antes de su fin NO debe conservar la fecha futura: la planilla
        // (que liquida CESADO con contrato FINALIZADO solapado) pagaría meses posteriores al cese.
        java.time.LocalDate hoy = java.time.LocalDate.now();
        if (contract.getFechaFin() == null || contract.getFechaFin().isAfter(hoy)) contract.setFechaFin(hoy);
        Contract updated = contractRepository.save(contract);

        // Sin otro contrato ACTIVO el empleado queda CESADO: la planilla sólo liquida ACTIVOS.
        Employee employee = contract.getEmployee();
        boolean quedaOtroActivo = contractRepository.findByTenantIdAndEmployeeId(tenantId, employee.getId()).stream()
                .anyMatch(c -> !c.getId().equals(id) && c.getEstado() == Contract.ContractStatus.ACTIVO);
        if (!quedaOtroActivo && employee.getEstado() == Employee.EmployeeStatus.ACTIVO) {
            employee.setEstado(Employee.EmployeeStatus.CESADO);
            employeeRepository.save(employee);
        }
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
        reactivarSiCesado(oldContract.getEmployee());
        sincronizarSalario(oldContract.getEmployee(), saved);

        log.info("Contrato {} renovado con nuevo contrato {} - Tenant: {}", id, saved.getId(), tenantId);
        return contractMapper.toDto(saved);
    }

    /** Un contrato nuevo/renovado reincorpora a quien un fin de contrato dejó CESADO. */
    private void reactivarSiCesado(Employee employee) {
        if (employee.getEstado() == Employee.EmployeeStatus.CESADO) {
            employee.setEstado(Employee.EmployeeStatus.ACTIVO);
            employeeRepository.save(employee);
        }
    }

    /**
     * La planilla prioriza el Salary abierto sobre el contrato; sin esto, renovar a 2500 seguiría
     * pagando 2000. Reusa createSalaryRecord (cierra el abierto con fechaFin = inicio - 1 y abre el nuevo).
     */
    private void sincronizarSalario(Employee employee, Contract contract) {
        boolean yaVigente = employee.getSalaries().stream()
                .anyMatch(s -> s.getFechaFin() == null && s.getSalarioBase().compareTo(contract.getSalarioBase()) == 0);
        if (yaVigente) {
            return;
        }
        employeeSubResourceCommandService.createSalaryRecord(employee.getId(), SalaryDto.Request.builder()
                .fechaInicio(contract.getFechaInicio())
                .salarioBase(contract.getSalarioBase())
                .moneda(contract.getMoneda())
                .build());
    }
}
