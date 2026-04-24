package com.microshop.rrhh.application.query;

import com.microshop.rrhh.application.dto.attendance.AttendanceResponseDto;
import com.microshop.rrhh.application.dto.attendance.AttendanceSummaryDto;
import com.microshop.rrhh.application.mapper.AttendanceMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Attendance;
import com.microshop.rrhh.infrastructure.persistence.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttendanceQueryService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final TenantContext tenantContext;

    public List<AttendanceResponseDto> getAttendanceByDate(LocalDate fecha) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return attendanceRepository.findByTenantIdAndFecha(tenantId, fecha).stream()
                .map(attendanceMapper::toDto)
                .toList();
    }

    public List<AttendanceResponseDto> getAttendanceByEmployee(Long employeeId) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return attendanceRepository.findByTenantIdAndEmployee_Id(tenantId, employeeId).stream()
                .map(attendanceMapper::toDto)
                .toList();
    }

    public List<AttendanceResponseDto> getMonthlyReport(Long employeeId, YearMonth month) {
        Long tenantId = tenantContext.getCurrentTenantId();
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return attendanceRepository.findByTenantIdAndEmployeeIdAndFechaBetween(tenantId, employeeId, start, end).stream()
                .map(attendanceMapper::toDto)
                .toList();
    }

    public List<AttendanceSummaryDto> getMonthlySummary(YearMonth month) {
        Long tenantId = tenantContext.getCurrentTenantId();
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        // Get all attendance for the month
        List<Attendance> allAttendance = attendanceRepository.findByTenantId(tenantId).stream()
                .filter(a -> !a.getFecha().isBefore(start) && !a.getFecha().isAfter(end))
                .toList();

        // Group by employee
        Map<Long, List<Attendance>> grouped = allAttendance.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<Attendance> records = entry.getValue();
                    Attendance first = records.getFirst();
                    String empName = first.getEmployee().getNombres() + " " + first.getEmployee().getApellidos();

                    int diasTrabajados = (int) records.stream()
                            .filter(a -> a.getTipoRegistro() == Attendance.AttendanceType.NORMAL ||
                                         a.getTipoRegistro() == Attendance.AttendanceType.TARDANZA)
                            .count();
                    int tardanzas = (int) records.stream()
                            .filter(a -> a.getTipoRegistro() == Attendance.AttendanceType.TARDANZA).count();
                    int faltas = (int) records.stream()
                            .filter(a -> a.getTipoRegistro() == Attendance.AttendanceType.FALTA).count();
                    int permisos = (int) records.stream()
                            .filter(a -> a.getTipoRegistro() == Attendance.AttendanceType.PERMISO).count();

                    BigDecimal totalHoras = records.stream()
                            .map(a -> a.getHorasTrabajadas() != null ? a.getHorasTrabajadas() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal totalExtras = records.stream()
                            .map(a -> a.getHorasExtras() != null ? a.getHorasExtras() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return AttendanceSummaryDto.builder()
                            .employeeId(entry.getKey())
                            .employeeName(empName)
                            .diasTrabajados(diasTrabajados)
                            .tardanzas(tardanzas)
                            .faltas(faltas)
                            .permisos(permisos)
                            .totalHorasTrabajadas(totalHoras)
                            .totalHorasExtras(totalExtras)
                            .build();
                })
                .toList();
    }

    public long countTodayAttendance() {
        return attendanceRepository.countByTenantIdAndFecha(
                tenantContext.getCurrentTenantId(), LocalDate.now());
    }
}
