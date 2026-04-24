package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.training.TrainingParticipationRequestDto;
import com.microshop.rrhh.application.dto.training.TrainingParticipationResponseDto;
import com.microshop.rrhh.domain.model.TrainingParticipation;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TrainingParticipationMapper {

    public TrainingParticipation toEntity(TrainingParticipationRequestDto dto, Long tenantId) {
        return TrainingParticipation.builder()
                .tenantId(tenantId)
                .trainingId(dto.trainingId())
                .employeeId(dto.employeeId())
                .fechaInscripcion(LocalDate.now())
                .estado(TrainingParticipation.ParticipationStatus.INSCRITO)
                .asistenciaPorcentaje(dto.asistenciaPorcentaje())
                .notaFinal(dto.notaFinal())
                .comentarios(dto.comentarios())
                .build();
    }

    public void updateEntity(TrainingParticipation entity, TrainingParticipationRequestDto dto) {
        if (dto.asistenciaPorcentaje() != null) {
            entity.setAsistenciaPorcentaje(dto.asistenciaPorcentaje());
        }
        if (dto.notaFinal() != null) {
            entity.setNotaFinal(dto.notaFinal());
        }
        if (dto.comentarios() != null) {
            entity.setComentarios(dto.comentarios());
        }
    }

    public TrainingParticipationResponseDto toDto(TrainingParticipation entity, String trainingName, String employeeName) {
        return TrainingParticipationResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .trainingId(entity.getTrainingId())
                .trainingName(trainingName)
                .employeeId(entity.getEmployeeId())
                .employeeName(employeeName)
                .fechaInscripcion(entity.getFechaInscripcion())
                .estado(entity.getEstado() != null ? entity.getEstado().name() : null)
                .asistenciaPorcentaje(entity.getAsistenciaPorcentaje())
                .notaFinal(entity.getNotaFinal())
                .aprobado(entity.getAprobado())
                .certificadoEmitido(entity.getCertificadoEmitido())
                .comentarios(entity.getComentarios())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
