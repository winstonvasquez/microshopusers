package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.attendance.AttendanceRequestDto;
import com.microshop.rrhh.application.dto.attendance.AttendanceResponseDto;
import com.microshop.rrhh.application.dto.attendance.CheckInOutDto;
import com.microshop.rrhh.application.mapper.AttendanceMapper;
import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Attendance;
import com.microshop.rrhh.domain.model.Employee;
import com.microshop.rrhh.infrastructure.persistence.repository.AttendanceRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class AttendanceCommandService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceMapper attendanceMapper;
    private final TenantContext tenantContext;
    private final MessageHelper msg;

    public AttendanceResponseDto registerAttendance(@Valid AttendanceRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(request.employeeId(), tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("attendance.employee.not.found")));

        attendanceRepository.findByTenantIdAndEmployee_IdAndFecha(
                tenantId, request.employeeId(), request.fecha())
                .ifPresent(a -> {
                    throw new ConflictException(msg.get("attendance.duplicate", request.employeeId(), request.fecha()));
                });

        Attendance attendance = attendanceMapper.toEntity(request, tenantId, employee);
        Attendance saved = attendanceRepository.save(attendance);

        log.info("Asistencia registrada: {} - Empleado: {} - Tenant: {}",
                saved.getId(), request.employeeId(), tenantId);
        return attendanceMapper.toDto(saved);
    }

    public AttendanceResponseDto checkIn(@Valid CheckInOutDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        LocalDate today = LocalDate.now();

        Employee employee = employeeRepository.findByIdAndTenantId(request.employeeId(), tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("attendance.employee.not.found")));

        // Check if already checked in today
        var existing = attendanceRepository.findByTenantIdAndEmployee_IdAndFecha(tenantId, request.employeeId(), today);
        if (existing.isPresent()) {
            throw new ConflictException(msg.get("attendance.duplicate", request.employeeId(), today));
        }

        LocalTime now = LocalTime.now();
        Attendance.AttendanceType tipo = now.isAfter(LocalTime.of(9, 0))
                ? Attendance.AttendanceType.TARDANZA
                : Attendance.AttendanceType.NORMAL;

        Attendance attendance = Attendance.builder()
                .tenantId(tenantId)
                .employee(employee)
                .fecha(today)
                .horaEntrada(now)
                .tipoRegistro(tipo)
                .ubicacionEntrada(request.ubicacion())
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Check-in: empleado {} a las {} - Tenant: {}", request.employeeId(), now, tenantId);
        return attendanceMapper.toDto(saved);
    }

    public AttendanceResponseDto checkOut(@Valid CheckInOutDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByTenantIdAndEmployee_IdAndFecha(tenantId, request.employeeId(), today)
                .orElseThrow(() -> new BusinessException(msg.get("attendance.no.checkin")));

        if (attendance.getHoraSalida() != null) {
            throw new ConflictException(msg.get("attendance.already.checkout"));
        }

        attendance.setHoraSalida(LocalTime.now());
        attendance.setUbicacionSalida(request.ubicacion());

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Check-out: empleado {} a las {} - Tenant: {}", request.employeeId(), saved.getHoraSalida(), tenantId);
        return attendanceMapper.toDto(saved);
    }
}
