package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.vacation.VacationRequestDto;
import com.microshop.rrhh.application.dto.vacation.VacationResponseDto;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.VacationRequest;
import com.microshop.users.shared.util.AppUtils;
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
                .employeeId(AppUtils.idOrNull(entity.getEmployee(), e -> e.getId()))
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .dias(entity.getDias())
                .estado(entity.getEstado())
                .motivo(entity.getMotivo())
                .aprobadoPor(AppUtils.idOrNull(entity.getAprobadoPor(), e -> e.getId()))
                .fechaAprobacion(entity.getFechaAprobacion())
                .comentariosAprobacion(entity.getComentariosAprobacion())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDate() : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toLocalDate() : null)
                .build();
    }
}
