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
import com.microshop.users.shared.util.AppUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    /** Sobrecarga corta (compat): mantiene la firma previa para llamadores que no filtran por search/instructor/fechaFin. */
    public Page<TrainingResponseDto> getTrainingsPaged(Training.TrainingStatus estado, LocalDate fechaInicioDesde,
                                                        LocalDate fechaInicioHasta, Pageable pageable) {
        return getTrainingsPaged(null, estado, null, fechaInicioDesde, fechaInicioHasta, null, null, pageable);
    }

    /** Listado paginado server-side: búsqueda por texto + estado + instructor + rango fechaInicio/fechaFin. */
    public Page<TrainingResponseDto> getTrainingsPaged(String search, Training.TrainingStatus estado, String instructor,
                                                        LocalDate fechaInicioDesde, LocalDate fechaInicioHasta,
                                                        LocalDate fechaFinDesde, LocalDate fechaFinHasta,
                                                        Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        String searchParam = blankToEmpty(search);
        String instructorParam = blankToEmpty(instructor);
        return trainingRepository.searchPaged(tenantId, searchParam, estado, instructorParam,
                        fechaInicioDesde, fechaInicioHasta, fechaFinDesde, fechaFinHasta, pageable)
                .map(t -> trainingMapper.toDto(t, participationRepository.countByTenantIdAndTrainingId(tenantId, t.getId())));
    }

    /** Normaliza un parámetro String: null o en blanco -> cadena vacía (centinela usado en la query). */
    private static String blankToEmpty(String value) {
        return (value != null && !value.isBlank()) ? value : "";
    }

    public List<TrainingParticipationResponseDto> getParticipantsByTraining(Long trainingId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        String trainingName = trainingRepository.findByIdAndTenantId(trainingId, tenantId)
                .map(Training::getNombre).orElse(null);

        return participationRepository.findByTenantIdAndTrainingId(tenantId, trainingId).stream()
                .map(p -> {
                    String empName = employeeRepository.findByIdAndTenantId(p.getEmployeeId(), tenantId)
                            .map(e -> AppUtils.fullName(e.getNombres(), e.getApellidos())).orElse(null);
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
                            .map(e -> AppUtils.fullName(e.getNombres(), e.getApellidos())).orElse(null);
                    return participationMapper.toDto(p, trainingName, empName);
                })
                .toList();
    }
}
