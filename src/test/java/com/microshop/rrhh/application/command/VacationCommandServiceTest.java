package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.vacation.VacationRequestDto;
import com.microshop.rrhh.application.mapper.VacationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.domain.model.VacationRequest;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.VacationRequestRepository;
import com.microshop.users.application.MessageHelper;
import com.microshop.users.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VacationCommandServiceTest {

    private final VacationRequestRepository repo = mock(VacationRequestRepository.class);
    private final EmployeeRepository employees = mock(EmployeeRepository.class);
    private final LeaveBalanceCommandService balances = mock(LeaveBalanceCommandService.class);
    private final VacationMapper mapper = mock(VacationMapper.class);
    private final TenantContext tenant = mock(TenantContext.class);
    private final VacationCommandService service = new VacationCommandService(
            repo, employees, balances, mapper, tenant, mock(MessageHelper.class));

    @Test
    void solapamientoRechazado() {
        Employee emp = Employee.builder().id(5L).build();
        when(tenant.getCurrentTenantId()).thenReturn(1L);
        when(employees.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(emp));
        when(repo.existsSolapada(anyLong(), anyLong(), any(), any(), any())).thenReturn(true);
        var dto = new VacationRequestDto(5L, LocalDate.now(), LocalDate.now().plusDays(4), 5, null, null);

        assertThatThrownBy(() -> service.createVacationRequest(dto)).isInstanceOf(BusinessException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void cancelarAprobadaDevuelveDias() {
        Employee emp = Employee.builder().id(5L).build();
        VacationRequest v = VacationRequest.builder().id(9L).employee(emp).dias(7)
                .fechaInicio(LocalDate.of(2026, 3, 2)).fechaFin(LocalDate.of(2026, 3, 8))
                .estado(VacationRequest.VacationStatus.APROBADO).build();
        when(tenant.getCurrentTenantId()).thenReturn(1L);
        when(repo.findByIdAndTenantId(9L, 1L)).thenReturn(Optional.of(v));
        when(repo.save(v)).thenReturn(v);

        service.cancelVacation(9L);

        assertThat(v.getEstado()).isEqualTo(VacationRequest.VacationStatus.CANCELADO);
        verify(balances).restoreBalance(5L, 7, 2026);
    }

    @Test
    void cancelarTerminalRechazada() {
        VacationRequest v = VacationRequest.builder().id(9L)
                .estado(VacationRequest.VacationStatus.RECHAZADO).build();
        when(tenant.getCurrentTenantId()).thenReturn(1L);
        when(repo.findByIdAndTenantId(9L, 1L)).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> service.cancelVacation(9L)).isInstanceOf(BusinessException.class);
        verify(balances, never()).restoreBalance(anyLong(), anyInt(), anyInt());
    }
}
