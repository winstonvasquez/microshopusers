package com.microshop.rrhh.application.mapper;

import com.microshop.rrhh.application.dto.contract.ContractRequestDto;
import com.microshop.rrhh.application.dto.contract.ContractResponseDto;
import com.microshop.rrhh.domain.model.Contract;
import com.microshop.rrhh.domain.model.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ContractMapper {

    public Contract toEntity(ContractRequestDto dto, Long tenantId) {
        return Contract.builder()
                .tenantId(tenantId)
                .tipoContrato(dto.tipoContrato())
                .fechaInicio(dto.fechaInicio())
                .fechaFin(dto.fechaFin())
                .salarioBase(dto.salarioBase())
                .moneda(dto.moneda() != null ? dto.moneda() : "PEN")
                .jornadaLaboral(dto.jornadaLaboral())
                .horasSemanales(dto.horasSemanales())
                .periodoPruebaMeses(dto.periodoPruebaMeses())
                .documentoContratoUrl(dto.documentoContratoUrl())
                .estado(Contract.ContractStatus.ACTIVO)
                .build();
    }

    public void updateEntity(Contract entity, ContractRequestDto dto) {
        entity.setTipoContrato(dto.tipoContrato());
        entity.setFechaInicio(dto.fechaInicio());
        entity.setFechaFin(dto.fechaFin());
        entity.setSalarioBase(dto.salarioBase());
        if (dto.moneda() != null) entity.setMoneda(dto.moneda());
        entity.setJornadaLaboral(dto.jornadaLaboral());
        entity.setHorasSemanales(dto.horasSemanales());
        entity.setPeriodoPruebaMeses(dto.periodoPruebaMeses());
        entity.setDocumentoContratoUrl(dto.documentoContratoUrl());
    }

    public ContractResponseDto toDto(Contract entity) {
        Employee emp = entity.getEmployee();
        boolean expiringSoon = entity.getFechaFin() != null
                && entity.getEstado() == Contract.ContractStatus.ACTIVO
                && entity.getFechaFin().isBefore(LocalDate.now().plusDays(30));

        return ContractResponseDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .employeeId(emp != null ? emp.getId() : null)
                .employeeName(emp != null ? emp.getNombres() + " " + emp.getApellidos() : null)
                .tipoContrato(entity.getTipoContrato())
                .fechaInicio(entity.getFechaInicio())
                .fechaFin(entity.getFechaFin())
                .salarioBase(entity.getSalarioBase())
                .moneda(entity.getMoneda())
                .jornadaLaboral(entity.getJornadaLaboral())
                .horasSemanales(entity.getHorasSemanales())
                .periodoPruebaMeses(entity.getPeriodoPruebaMeses())
                .documentoContratoUrl(entity.getDocumentoContratoUrl())
                .estado(entity.getEstado())
                .motivoFin(entity.getMotivoFin())
                .expiringSoon(expiringSoon)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
