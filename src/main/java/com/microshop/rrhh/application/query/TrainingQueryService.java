package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.training.TrainingParticipationResponseDto;
import com.microshop.rrhh.application.dto.training.TrainingResponseDto;
import com.microshop.rrhh.application.mapper.TrainingMapper;
import com.microshop.rrhh.application.mapper.TrainingParticipationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Training;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.TrainingParticipationRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.TrainingRepository;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrainingQueryService {

    private final TrainingRepository trainingRepository;
    private final TrainingParticipationRepository participationRepository;
    private final EmployeeRepository employeeRepository;
    private final TrainingMapper trainingMapper;
    private final TrainingParticipationMapper participationMapper;
    private final TenantContext tenantContext;

    public List<TrainingResponseDto> getAll() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return trainingRepository.findByTenantId(tenantId).stream()
                .map(t -> trainingMapper.toDto(t, participationRepository.countByTenantIdAndTrainingId(tenantId, t.getId())))
                .toList();
    }

    public TrainingResponseDto getById(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Training training = trainingRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Capacitación no encontrada"));
        long count = participationRepository.countByTenantIdAndTrainingId(tenantId, id);
        return trainingMapper.toDto(training, count);
    }

    public List<TrainingResponseDto> getByStatus(String estado) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return trainingRepository.findByTenantIdAndEstado(tenantId, Training.TrainingStatus.valueOf(estado)).stream()
                .map(t -> trainingMapper.toDto(t, participationRepository.countByTenantIdAndTrainingId(tenantId, t.getId())))
                .toList();
    }

    public List<TrainingParticipationResponseDto> getParticipantsByTraining(Long trainingId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        String trainingName = trainingRepository.findByIdAndTenantId(trainingId, tenantId)
                .map(Training::getNombre).orElse(null);

        return participationRepository.findByTenantIdAndTrainingId(tenantId, trainingId).stream()
                .map(p -> {
                    String empName = employeeRepository.findByIdAndTenantId(p.getEmployeeId(), tenantId)
                            .map(e -> e.getNombres() + " " + e.getApellidos()).orElse(null);
                    return participationMapper.toDto(p, trainingName, empName);
                })
                .toList();
    }

    public List<TrainingParticipationResponseDto> getParticipationsByEmployee(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return participationRepository.findByTenantIdAndEmployeeId(tenantId, employeeId).stream()
                .map(p -> {
                    String trainingName = trainingRepository.findByIdAndTenantId(p.getTrainingId(), tenantId)
                            .map(Training::getNombre).orElse(null);
                    String empName = employeeRepository.findByIdAndTenantId(p.getEmployeeId(), tenantId)
                            .map(e -> e.getNombres() + " " + e.getApellidos()).orElse(null);
                    return participationMapper.toDto(p, trainingName, empName);
                })
                .toList();
    }
}
