package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.mapper.ContractMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Contract;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.infrastructure.persistence.repository.ContractRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.users.application.MessageHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** R2P-1: terminar un plazo fijo antes de su fin no debe dejar la fecha futura (la planilla pagaría de más). */
@ExtendWith(MockitoExtension.class)
class ContractTerminateFechaFinTest {

    @Mock private ContractRepository contractRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ContractMapper contractMapper;
    @Mock private TenantContext tenantContext;
    @Mock private MessageHelper msg;
    @Mock private EmployeeSubResourceCommandService employeeSubResourceCommandService;
    @InjectMocks private ContractCommandService service;

    private Contract terminar(LocalDate fechaFin) {
        Employee e = new Employee();
        e.setId(1L);
        e.setEstado(Employee.EmployeeStatus.ACTIVO);
        Contract c = new Contract();
        c.setId(10L);
        c.setEmployee(e);
        c.setEstado(Contract.ContractStatus.ACTIVO);
        c.setFechaFin(fechaFin);
        when(tenantContext.getCurrentTenantId()).thenReturn(1L);
        when(contractRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(c));
        when(contractRepository.save(any(Contract.class))).thenAnswer(i -> i.getArgument(0));
        when(contractRepository.findByTenantIdAndEmployeeId(1L, 1L)).thenReturn(List.of(c));
        service.terminateContract(10L, "renuncia");
        return c;
    }

    @Test
    void plazoFijoConFinFuturoQuedaCortadoHoy() {
        assertThat(terminar(LocalDate.now().plusMonths(2)).getFechaFin()).isEqualTo(LocalDate.now());
    }

    @Test
    void sinFinSeFijaHoy() {
        assertThat(terminar(null).getFechaFin()).isEqualTo(LocalDate.now());
    }

    @Test
    void finYaVencidoSeConserva() {
        LocalDate pasado = LocalDate.now().minusDays(5);
        assertThat(terminar(pasado).getFechaFin()).isEqualTo(pasado);
    }
}
