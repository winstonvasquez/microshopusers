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
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class VacationCommandService {

    private static final List<VacationRequest.VacationStatus> ESTADOS_VIVOS =
            List.of(VacationRequest.VacationStatus.SOLICITADO, VacationRequest.VacationStatus.APROBADO);

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

        return persistirSolicitud(request, employee, tenantId);
    }

    /**
     * Autoservicio: crea la solicitud SIEMPRE a nombre del empleado vinculado al usuario
     * autenticado (JWT). El {@code employeeId} que venga en el body se IGNORA por completo,
     * de modo que un empleado no puede registrar vacaciones a nombre de otro.
     */
    public VacationResponseDto createOwnVacationRequest(@Valid VacationRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Long userId = tenantContext.getCurrentUserId();
        if (userId == null) {
            throw new NotFoundException("El usuario no tiene un empleado asociado");
        }

        Employee employee = employeeRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new NotFoundException("El usuario no tiene un empleado asociado"));

        return persistirSolicitud(request, employee, tenantId);
    }

    /** Validación de fechas + persistencia común a la creación admin y de autoservicio. */
    private VacationResponseDto persistirSolicitud(VacationRequestDto request, Employee employee, Long tenantId) {
        if (request.fechaFin().isBefore(request.fechaInicio())) {
            throw new BusinessException(msg.get("vacation.fechaFin.invalid"));
        }

        if (vacationRequestRepository.existsSolapada(tenantId, employee.getId(), ESTADOS_VIVOS,
                request.fechaInicio(), request.fechaFin())) {
            throw new BusinessException("El empleado ya tiene una solicitud de vacaciones que se solapa con ese rango");
        }

        // El mapper toma el empleado del parámetro, no de request.employeeId().
        VacationRequest vacation = vacationMapper.toEntity(request, tenantId, employee);
        VacationRequest saved = vacationRequestRepository.save(vacation);

        log.info("Solicitud de vacaciones creada: {} - Empleado: {} - Tenant: {}",
                saved.getId(), employee.getId(), tenantId);
        return vacationMapper.toDto(saved);
    }

    public VacationResponseDto approveOrRejectVacation(Long id, @Valid VacationApprovalDto approval) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Long userId = tenantContext.getCurrentUserId();

        VacationRequest vacation = vacationRequestRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("vacation.not.found")));

        if (userId == null) {
            throw new NotFoundException(msg.get("vacation.aprobador.not.found"));
        }
        // El aprobador es un ADMIN del tenant: si no tiene ficha de empleado se aprueba con aprobadoPor null.
        Employee aprobador = employeeRepository.findByTenantIdAndUserId(tenantId, userId).orElse(null);
        if (aprobador != null && aprobador.getId().equals(vacation.getEmployee().getId())) {
            throw new BusinessException("No puede aprobar ni rechazar su propia solicitud de vacaciones");
        }

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

    /** SOLICITADO/APROBADO → CANCELADO; si estaba APROBADO devuelve los días al saldo. */
    public VacationResponseDto cancelVacation(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        VacationRequest vacation = vacationRequestRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("vacation.not.found")));

        VacationRequest.VacationStatus previo = vacation.getEstado();
        if (!ESTADOS_VIVOS.contains(previo)) {
            throw new BusinessException("Solo se pueden cancelar solicitudes SOLICITADO o APROBADO");
        }

        vacation.setEstado(VacationRequest.VacationStatus.CANCELADO);
        VacationRequest updated = vacationRequestRepository.save(vacation);

        if (previo == VacationRequest.VacationStatus.APROBADO) {
            leaveBalanceCommandService.restoreBalance(
                    vacation.getEmployee().getId(), vacation.getDias(), vacation.getFechaInicio().getYear());
        }

        log.info("Solicitud de vacaciones cancelada - ID: {} - Tenant: {}", id, tenantId);
        return vacationMapper.toDto(updated);
    }
}
