package com.microshop.rrhh.application.command;

import com.microshop.rrhh.domain.model.Contract;
import com.microshop.rrhh.domain.model.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class PayrollContratoVigenteTest {

    private static Employee cesadoElDia15() {
        Employee e = new Employee();
        e.setEstado(Employee.EmployeeStatus.CESADO);
        e.setContracts(new ArrayList<>());
        Contract c = new Contract();
        c.setEstado(Contract.ContractStatus.FINALIZADO);
        c.setFechaInicio(LocalDate.of(2026, 1, 1));
        c.setFechaFin(LocalDate.of(2026, 9, 15));
        e.getContracts().add(c);
        return e;
    }

    @Test
    @DisplayName("Cesado el 15: entra en la planilla de ese mes y no en la siguiente")
    void cesadoMitadDeMes() {
        Employee e = cesadoElDia15();
        assertThat(PayrollCommandService.tieneContratoVigente(e,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))).isTrue();
        assertThat(PayrollCommandService.tieneContratoVigente(e,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31))).isFalse();
    }

    @Test
    @DisplayName("Cesado sin contratos no se liquida; activo sin contratos sí")
    void sinContratos() {
        Employee cesado = new Employee();
        cesado.setEstado(Employee.EmployeeStatus.CESADO);
        cesado.setContracts(new ArrayList<>());
        Employee activo = new Employee();
        activo.setEstado(Employee.EmployeeStatus.ACTIVO);
        activo.setContracts(new ArrayList<>());
        LocalDate i = LocalDate.of(2026, 9, 1), f = LocalDate.of(2026, 9, 30);
        assertThat(PayrollCommandService.tieneContratoVigente(cesado, i, f)).isFalse();
        assertThat(PayrollCommandService.tieneContratoVigente(activo, i, f)).isTrue();
    }
}
