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
                // Los días se derivan del rango, NUNCA del cliente: al aprobar se
                // descuenta esa cantidad del saldo, así que un `dias` menor al
                // rango real regalaría ausencia sin consumir saldo.
                .dias(diasEntre(dto.fechaInicio(), dto.fechaFin()))
                .motivo(dto.motivo())
                .tipoVacacion(dto.tipoVacacion() != null ? dto.tipoVacacion() : VacationRequest.VacationType.ANUAL)
                .estado(VacationRequest.VacationStatus.SOLICITADO)
                .build();
    }

    /** Días calendario que abarca la solicitud, extremos incluidos. */
    public static int diasEntre(java.time.LocalDate desde, java.time.LocalDate hasta) {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(desde, hasta) + 1;
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
                .tipoVacacion(entity.getTipoVacacion())
                .motivo(entity.getMotivo())
                .aprobadoPor(AppUtils.idOrNull(entity.getAprobadoPor(), e -> e.getId()))
                .fechaAprobacion(entity.getFechaAprobacion())
                .comentariosAprobacion(entity.getComentariosAprobacion())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDate() : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toLocalDate() : null)
                .build();
    }
}
