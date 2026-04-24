package com.microshop.rrhh.application.command;

import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.application.dto.training.*;
import com.microshop.rrhh.application.mapper.TrainingMapper;
import com.microshop.rrhh.application.mapper.TrainingParticipationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.Training;
import com.microshop.rrhh.domain.model.TrainingParticipation;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.TrainingParticipationRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.TrainingRepository;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class TrainingCommandService {

    private final TrainingRepository trainingRepository;
    private final TrainingParticipationRepository participationRepository;
    private final EmployeeRepository employeeRepository;
    private final TrainingMapper trainingMapper;
    private final TrainingParticipationMapper participationMapper;
    private final TenantContext tenantContext;
    private final MessageHelper msg;

    // ── Training CRUD ───────────────────────────────────────────────────────

    public TrainingResponseDto createTraining(@Valid TrainingRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Training training = trainingMapper.toEntity(request, tenantId);
        Training saved = trainingRepository.save(training);
        log.info("Capacitación creada: {} - {} - Tenant: {}", saved.getId(), saved.getNombre(), tenantId);
        return trainingMapper.toDto(saved, 0);
    }

    public TrainingResponseDto updateTraining(Long id, @Valid TrainingRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Training training = trainingRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Capacitación no encontrada"));
        trainingMapper.updateEntity(training, request);
        Training saved = trainingRepository.save(training);
        long count = participationRepository.countByTenantIdAndTrainingId(tenantId, id);
        log.info("Capacitación actualizada: {} - Tenant: {}", id, tenantId);
        return trainingMapper.toDto(saved, count);
    }

    public TrainingResponseDto startTraining(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Training training = trainingRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Capacitación no encontrada"));

        if (training.getEstado() != Training.TrainingStatus.PLANIFICADO) {
            throw new BusinessException("Solo se pueden iniciar capacitaciones en estado PLANIFICADO");
        }

        training.setEstado(Training.TrainingStatus.EN_CURSO);
        Training saved = trainingRepository.save(training);
        long count = participationRepository.countByTenantIdAndTrainingId(tenantId, id);
        log.info("Capacitación iniciada: {} - Tenant: {}", id, tenantId);
        return trainingMapper.toDto(saved, count);
    }

    public TrainingResponseDto completeTraining(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Training training = trainingRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Capacitación no encontrada"));

        if (training.getEstado() != Training.TrainingStatus.EN_CURSO) {
            throw new BusinessException("Solo se pueden completar capacitaciones en estado EN_CURSO");
        }

        training.setEstado(Training.TrainingStatus.COMPLETADO);
        Training saved = trainingRepository.save(training);
        long count = participationRepository.countByTenantIdAndTrainingId(tenantId, id);
        log.info("Capacitación completada: {} - Tenant: {}", id, tenantId);
        return trainingMapper.toDto(saved, count);
    }

    public TrainingResponseDto cancelTraining(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Training training = trainingRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Capacitación no encontrada"));

        training.setEstado(Training.TrainingStatus.CANCELADO);
        Training saved = trainingRepository.save(training);
        long count = participationRepository.countByTenantIdAndTrainingId(tenantId, id);
        log.info("Capacitación cancelada: {} - Tenant: {}", id, tenantId);
        return trainingMapper.toDto(saved, count);
    }

    // ── Participation ───────────────────────────────────────────────────────

    public TrainingParticipationResponseDto enrollParticipant(@Valid TrainingParticipationRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        trainingRepository.findByIdAndTenantId(request.trainingId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Capacitación no encontrada"));

        Employee employee = employeeRepository.findByIdAndTenantId(request.employeeId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        participationRepository.findByTenantIdAndTrainingIdAndEmployeeId(tenantId, request.trainingId(), request.employeeId())
                .ifPresent(p -> { throw new ConflictException("El empleado ya está inscrito en esta capacitación"); });

        TrainingParticipation participation = participationMapper.toEntity(request, tenantId);
        TrainingParticipation saved = participationRepository.save(participation);
        String empName = employee.getNombres() + " " + employee.getApellidos();
        log.info("Participante inscrito: emp={} training={} - Tenant: {}", request.employeeId(), request.trainingId(), tenantId);
        return participationMapper.toDto(saved, null, empName);
    }

    public TrainingParticipationResponseDto updateParticipation(Long id, @Valid TrainingParticipationRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        TrainingParticipation participation = participationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Participación no encontrada"));

        participationMapper.updateEntity(participation, request);

        if (request.notaFinal() != null) {
            participation.setAprobado(request.notaFinal().compareTo(new BigDecimal("60")) >= 0);
        }

        TrainingParticipation saved = participationRepository.save(participation);
        String empName = resolveEmployeeName(tenantId, saved.getEmployeeId());
        String trainingName = resolveTrainingName(tenantId, saved.getTrainingId());
        log.info("Participación actualizada: {} - Tenant: {}", id, tenantId);
        return participationMapper.toDto(saved, trainingName, empName);
    }

    public TrainingParticipationResponseDto issueCertificate(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        TrainingParticipation participation = participationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Participación no encontrada"));

        if (participation.getAprobado() == null || !participation.getAprobado()) {
            throw new BusinessException("Solo se pueden emitir certificados para participantes aprobados");
        }

        participation.setCertificadoEmitido(true);
        TrainingParticipation saved = participationRepository.save(participation);
        String empName = resolveEmployeeName(tenantId, saved.getEmployeeId());
        String trainingName = resolveTrainingName(tenantId, saved.getTrainingId());
        log.info("Certificado emitido: participación={} - Tenant: {}", id, tenantId);
        return participationMapper.toDto(saved, trainingName, empName);
    }

    private String resolveEmployeeName(Long tenantId, Long employeeId) {
        return employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                .map(e -> e.getNombres() + " " + e.getApellidos())
                .orElse(null);
    }

    private String resolveTrainingName(Long tenantId, Long trainingId) {
        return trainingRepository.findByIdAndTenantId(trainingId, tenantId)
                .map(Training::getNombre)
                .orElse(null);
    }
}
