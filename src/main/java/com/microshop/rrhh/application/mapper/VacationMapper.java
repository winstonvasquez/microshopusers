package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.vacation.VacationRequestDto;
import com.microshop.rrhh.application.dto.vacation.VacationResponseDto;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.VacationRequest;
import org.springframework.stereotype.Component;

@Component
public class VacationMapper {

    public VacationRequest toEntity(VacationRequestDto dto, Long tenantId, Employee employee) {
        return VacationRequest.builder()
                .tenantId(tenantId)
                .employee(employee)
                .fechaInicio(dto.fechaInicio())
                .fechaFin(dto.fechaFin())
                .dias(dto.dias())
                .motivo(dto.motivo())
                .estado(VacationRequest.VacationStatus.SOLICITADO)
                .build();
    }

    public VacationResponseDto toDto(VacationRequest entity) {
        return VacationResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .dias(entity.getDias())
                .estado(entity.getEstado())
                .motivo(entity.getMotivo())
                .aprobadoPor(entity.getAprobadoPor() != null ? entity.getAprobadoPor().getId() : null)
                .fechaAprobacion(entity.getFechaAprobacion())
                .comentariosAprobacion(entity.getComentariosAprobacion())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDate() : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toLocalDate() : null)
                .build();
    }
}
