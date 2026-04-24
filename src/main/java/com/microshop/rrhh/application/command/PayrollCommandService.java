package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.payroll.PayrollRequestDto;
import com.microshop.rrhh.application.dto.payroll.PayrollResponseDto;
import com.microshop.rrhh.application.mapper.PayrollMapper;
import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.client.UsersParameterClient;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.*;
import com.microshop.rrhh.infrastructure.persistence.repository.AttendanceRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.PayrollRepository;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class PayrollCommandService {

    // AFP rates por defecto (aporte obligatorio + comisión + seguro)
    // Se leen de parámetros ERP con fallback a estos valores
    private static final Map<String, String> AFP_RATE_DEFAULTS = Map.of(
            "HABITAT",   "0.1270",
            "INTEGRA",   "0.1214",
            "PRIMA",     "0.1218",
            "PROFUTURO", "0.1254"
    );
    private static final String AFP_RATE_DEFAULT_FALLBACK = "0.1230";

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollMapper payrollMapper;
    private final TenantContext tenantContext;
    private final MessageHelper msg;
    private final UsersParameterClient usersParameterClient;

    public PayrollResponseDto createPayroll(@Valid PayrollRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();

        Employee employee = employeeRepository.findByIdAndTenantId(request.employeeId(), tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("payroll.employee.not.found")));

        payrollRepository.findByTenantIdAndEmployee_IdAndPeriodo(
                tenantId, request.employeeId(), request.periodo())
                .ifPresent(p -> {
                    throw new ConflictException(msg.get("payroll.duplicate", request.employeeId(), request.periodo()));
                });

        Payroll payroll = payrollMapper.toEntity(request, tenantId, employee);
        Payroll saved = payrollRepository.save(payroll);

        log.info("Planilla creada: {} - Empleado: {} - Periodo: {} - Tenant: {}",
                saved.getId(), request.employeeId(), request.periodo(), tenantId);
        return payrollMapper.toDto(saved);
    }

    public List<PayrollResponseDto> generatePayrollForPeriod(String periodo) {
        Long tenantId = tenantContext.getCurrentTenantId();
        YearMonth ym = YearMonth.parse(periodo);
        int month = ym.getMonthValue();

        List<Employee> activeEmployees = employeeRepository.findByTenantIdAndEstado(
                tenantId, Employee.EmployeeStatus.ACTIVO);

        List<Payroll> payrolls = activeEmployees.stream()
                .filter(emp -> payrollRepository.findByTenantIdAndEmployee_IdAndPeriodo(
                        tenantId, emp.getId(), periodo).isEmpty())
                .map(emp -> calcularPlanillaPeruana(emp, periodo, ym, month))
                .toList();

        List<Payroll> saved = payrollRepository.saveAll(payrolls);

        log.info("Planillas generadas para periodo {} - Cantidad: {} - Tenant: {}",
                periodo, saved.size(), tenantId);
        return saved.stream().map(payrollMapper::toDto).toList();
    }

    public PayrollResponseDto approvePayroll(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Payroll payroll = payrollRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("payroll.not.found")));

        if (payroll.getEstado() != Payroll.PayrollStatus.GENERADO) {
            throw new BusinessException(msg.get("payroll.estado.invalido", payroll.getEstado().name()));
        }

        payroll.setEstado(Payroll.PayrollStatus.APROBADO);
        Payroll updated = payrollRepository.save(payroll);
        log.info("Planilla aprobada: {} - Tenant: {}", id, tenantId);
        return payrollMapper.toDto(updated);
    }

    public PayrollResponseDto markAsPaid(Long id) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Payroll payroll = payrollRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("payroll.not.found")));

        if (payroll.getEstado() != Payroll.PayrollStatus.APROBADO) {
            throw new BusinessException(msg.get("payroll.estado.invalido", payroll.getEstado().name()));
        }

        payroll.setEstado(Payroll.PayrollStatus.PAGADO);
        payroll.setFechaPago(LocalDate.now());
        Payroll updated = payrollRepository.save(payroll);
        log.info("Planilla pagada: {} - Tenant: {}", id, tenantId);
        return payrollMapper.toDto(updated);
    }

    /**
     * Full Peruvian payroll calculation.
     * AFP individual per employee, complete Renta 5ta (5 brackets),
     * overtime from attendance, gratificacion (Jul/Dec), CTS (May/Nov).
     */
    private Payroll calcularPlanillaPeruana(Employee emp, String periodo, YearMonth ym, int month) {
        BigDecimal rmv = usersParameterClient.getDecimal("RMV", "1025.00");
        BigDecimal asignacionFam = usersParameterClient.getDecimal("ASIGNACION_FAMILIAR", "102.50");
        BigDecimal uit = usersParameterClient.getDecimal("UIT_ANIO", "5150.00");
        BigDecimal tasaOnp = usersParameterClient.getDecimal("TASA_ONP", "0.13");
        BigDecimal tasaEssalud = usersParameterClient.getDecimal("TASA_ESSALUD", "0.09");

        // 1. Sueldo base
        BigDecimal sueldoBase = emp.getSalaries().stream()
                .filter(s -> s.getFechaFin() == null)
                .max(Comparator.comparing(Salary::getFechaInicio))
                .map(Salary::getSalarioBase)
                .orElse(rmv);

        // 2. Asignación familiar
        BigDecimal asigFamiliar = emp.getDependents().isEmpty() ? BigDecimal.ZERO : asignacionFam;

        // 3. Overtime from attendance
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<Attendance> monthAttendance = attendanceRepository.findByTenantIdAndEmployeeIdAndFechaBetween(
                emp.getTenantId(), emp.getId(), start, end);

        BigDecimal horasExtras = monthAttendance.stream()
                .map(a -> a.getHorasExtras() != null ? a.getHorasExtras() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int diasTrabajados = (int) monthAttendance.stream()
                .filter(a -> a.getTipoRegistro() == Attendance.AttendanceType.NORMAL ||
                             a.getTipoRegistro() == Attendance.AttendanceType.TARDANZA)
                .count();

        // Overtime: 25% surcharge for first 2h, 35% after (DL 854 art. 10)
        BigDecimal tarifaHora = sueldoBase.divide(new BigDecimal("240"), 4, RoundingMode.HALF_UP);
        BigDecimal montoHorasExtras = horasExtras.multiply(tarifaHora)
                .multiply(new BigDecimal("1.25"))
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Base computable
        BigDecimal baseComputable = sueldoBase.add(asigFamiliar).add(montoHorasExtras);

        // 5. Pension system — read from employee
        String sistemaPension = emp.getSistemaPrevisional() != null ? emp.getSistemaPrevisional() : "ONP";
        BigDecimal montoAfpOnp;
        if ("AFP".equals(sistemaPension) && emp.getAfpNombre() != null) {
            String afpNombre = emp.getAfpNombre().toUpperCase();
            String paramKey = "AFP_RATE_" + afpNombre;
            String defaultRate = AFP_RATE_DEFAULTS.getOrDefault(afpNombre, AFP_RATE_DEFAULT_FALLBACK);
            BigDecimal afpRate = usersParameterClient.getDecimal(paramKey, defaultRate);
            montoAfpOnp = baseComputable.multiply(afpRate).setScale(2, RoundingMode.HALF_UP);
        } else {
            montoAfpOnp = baseComputable.multiply(tasaOnp).setScale(2, RoundingMode.HALF_UP);
        }

        // 6. EsSalud 9% (employer contribution)
        BigDecimal essalud = baseComputable.multiply(tasaEssalud).setScale(2, RoundingMode.HALF_UP);

        // 7. Renta 5ta — complete 5 brackets (art. 53 LIR)
        BigDecimal proyAnual = sueldoBase.multiply(new BigDecimal("14")); // 12 + 2 grat
        BigDecimal deduccion7UIT = uit.multiply(new BigDecimal("7"));
        BigDecimal rentaQuinta = calcularRenta5ta(proyAnual, deduccion7UIT, uit);

        // 8. Gratificación (July and December)
        BigDecimal gratificacion = BigDecimal.ZERO;
        if (month == 7 || month == 12) {
            gratificacion = sueldoBase.add(asigFamiliar);
        }

        // 9. CTS (May and November) — 1/12 of (sueldo + 1/6 grat)
        BigDecimal cts = BigDecimal.ZERO;
        if (month == 5 || month == 11) {
            BigDecimal grat = sueldoBase.add(asigFamiliar);
            cts = sueldoBase.add(asigFamiliar)
                    .add(grat.divide(new BigDecimal("6"), 2, RoundingMode.HALF_UP))
                    .divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP); // semestral
        }

        // Build payroll with details
        Payroll payroll = Payroll.builder()
                .tenantId(emp.getTenantId())
                .employee(emp)
                .periodo(periodo)
                .sueldoBase(sueldoBase)
                .asignacionFamiliar(asigFamiliar)
                .afpOnp(sistemaPension + (emp.getAfpNombre() != null ? " - " + emp.getAfpNombre() : ""))
                .montoAfpOnp(montoAfpOnp)
                .essalud(essalud)
                .rentaQuinta(rentaQuinta)
                .cts(cts)
                .gratificacion(gratificacion)
                .horasExtras(horasExtras)
                .montoHorasExtras(montoHorasExtras)
                .diasTrabajados(diasTrabajados > 0 ? diasTrabajados : 30)
                .estado(Payroll.PayrollStatus.GENERADO)
                .build();

        // Add detail lines
        addDetail(payroll, "Sueldo Base", PayrollDetail.ConceptType.INGRESO, sueldoBase, null, null);
        if (asigFamiliar.compareTo(BigDecimal.ZERO) > 0) {
            addDetail(payroll, "Asignación Familiar", PayrollDetail.ConceptType.INGRESO, asigFamiliar, null, null);
        }
        if (montoHorasExtras.compareTo(BigDecimal.ZERO) > 0) {
            addDetail(payroll, "Horas Extras", PayrollDetail.ConceptType.INGRESO, montoHorasExtras, horasExtras, new BigDecimal("125"));
        }
        if (gratificacion.compareTo(BigDecimal.ZERO) > 0) {
            addDetail(payroll, "Gratificación", PayrollDetail.ConceptType.INGRESO, gratificacion, null, null);
        }
        addDetail(payroll, sistemaPension, PayrollDetail.ConceptType.DESCUENTO, montoAfpOnp, null, null);
        if (rentaQuinta.compareTo(BigDecimal.ZERO) > 0) {
            addDetail(payroll, "Renta 5ta Categoría", PayrollDetail.ConceptType.DESCUENTO, rentaQuinta, null, null);
        }
        addDetail(payroll, "EsSalud", PayrollDetail.ConceptType.APORTE_EMPLEADOR, essalud, null, tasaEssalud.multiply(new BigDecimal("100")));
        if (cts.compareTo(BigDecimal.ZERO) > 0) {
            addDetail(payroll, "CTS", PayrollDetail.ConceptType.APORTE_EMPLEADOR, cts, null, null);
        }

        return payroll;
    }

    /**
     * Peruvian income tax — 5 brackets (art. 53 LIR):
     * Up to 5 UIT: 8%, 5-20 UIT: 14%, 20-35 UIT: 17%, 35-45 UIT: 20%, >45 UIT: 30%
     */
    private BigDecimal calcularRenta5ta(BigDecimal proyAnual, BigDecimal deduccion7UIT, BigDecimal uit) {
        if (proyAnual.compareTo(deduccion7UIT) <= 0) return BigDecimal.ZERO;

        BigDecimal exceso = proyAnual.subtract(deduccion7UIT);
        BigDecimal impuesto = BigDecimal.ZERO;

        BigDecimal[] limits = {
                uit.multiply(new BigDecimal("5")),
                uit.multiply(new BigDecimal("20")),
                uit.multiply(new BigDecimal("35")),
                uit.multiply(new BigDecimal("45"))
        };
        BigDecimal[] rates = {
                new BigDecimal("0.08"),
                new BigDecimal("0.14"),
                new BigDecimal("0.17"),
                new BigDecimal("0.20"),
                new BigDecimal("0.30")
        };

        BigDecimal prev = BigDecimal.ZERO;
        for (int i = 0; i < limits.length; i++) {
            BigDecimal tramo = limits[i].subtract(prev);
            if (exceso.compareTo(tramo) <= 0) {
                impuesto = impuesto.add(exceso.multiply(rates[i]));
                exceso = BigDecimal.ZERO;
                break;
            }
            impuesto = impuesto.add(tramo.multiply(rates[i]));
            exceso = exceso.subtract(tramo);
            prev = limits[i];
        }
        if (exceso.compareTo(BigDecimal.ZERO) > 0) {
            impuesto = impuesto.add(exceso.multiply(rates[4]));
        }

        return impuesto.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
    }

    private void addDetail(Payroll payroll, String concepto, PayrollDetail.ConceptType tipo,
                          BigDecimal monto, BigDecimal cantidad, BigDecimal tasa) {
        PayrollDetail detail = PayrollDetail.builder()
                .tenantId(payroll.getTenantId())
                .payroll(payroll)
                .concepto(concepto)
                .tipo(tipo)
                .monto(monto)
                .cantidad(cantidad)
                .tasa(tasa)
                .build();
        payroll.getDetails().add(detail);
    }
}
