package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.vacation.VacationApprovalDto;
import com.microshop.rrhh.application.dto.vacation.VacationRequestDto;
import com.microshop.rrhh.application.dto.vacation.VacationResponseDto;
import com.microshop.rrhh.application.mapper.VacationMapper;
import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.VacationRequest;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.VacationRequestRepository;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class VacationCommandService {

    private final VacationRequestRepository vacationRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceCommandService leaveBalanceCommandService;
    private final VacationMapper vacationMapper;
    private final TenantContext tenantContext;
    private final MessageHelper msg;

    public VacationResponseDto createVacationRequest(@Valid VacationRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(request.employeeId(), tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("vacation.employee.not.found")));

        if (request.fechaFin().isBefore(request.fechaInicio())) {
            throw new BusinessException(msg.get("vacation.fechaFin.invalid"));
        }

        VacationRequest vacation = vacationMapper.toEntity(request, tenantId, employee);
        VacationRequest saved = vacationRequestRepository.save(vacation);

        log.info("Solicitud de vacaciones creada: {} - Empleado: {} - Tenant: {}",
                saved.getId(), request.employeeId(), tenantId);
        return vacationMapper.toDto(saved);
    }

    public VacationResponseDto approveOrRejectVacation(Long id, @Valid VacationApprovalDto approval) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Long userId = tenantContext.getCurrentUserId();

        VacationRequest vacation = vacationRequestRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("vacation.not.found")));

        Employee aprobador = employeeRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("vacation.aprobador.not.found")));

        if (vacation.getEstado() != VacationRequest.VacationStatus.SOLICITADO) {
            throw new BusinessException(msg.get("vacation.estado.invalido"));
        }

        boolean approved = approval.approved();

        vacation.setEstado(approved
                ? VacationRequest.VacationStatus.APROBADO
                : VacationRequest.VacationStatus.RECHAZADO);
        vacation.setAprobadoPor(aprobador);
        vacation.setFechaAprobacion(LocalDate.now());
        vacation.setComentariosAprobacion(approval.comentarios());

        VacationRequest updated = vacationRequestRepository.save(vacation);

        // Decrement leave balance on approval
        if (approved) {
            int year = vacation.getFechaInicio().getYear();
            leaveBalanceCommandService.decrementBalance(
                    vacation.getEmployee().getId(), vacation.getDias(), year);
        }

        log.info("Solicitud de vacaciones {} - ID: {} - Tenant: {}",
                approved ? "aprobada" : "rechazada", id, tenantId);
        return vacationMapper.toDto(updated);
    }
}
