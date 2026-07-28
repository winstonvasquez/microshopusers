package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.payroll.PayrollRequestDto;
import com.microshop.rrhh.application.dto.payroll.PayrollResponseDto;
import com.microshop.rrhh.application.mapper.PayrollMapper;
import com.microshop.users.application.MessageHelper;
import com.microshop.rrhh.client.ContabilidadClient;
import com.microshop.rrhh.client.TesoreriaClient;
import com.microshop.rrhh.client.UsersParameterClient;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.*;
import com.microshop.rrhh.infrastructure.persistence.repository.AttendanceRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.PayrollRepository;
import com.microshop.users.shared.constants.AppConstants;
import com.microshop.users.shared.exception.BusinessException;
import com.microshop.users.shared.exception.ConflictException;
import com.microshop.users.shared.exception.NotFoundException;
import com.microshop.users.shared.util.AppUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
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

    // Tasa AFP por defecto: se lee del parámetro ERP AFP_RATE_<NOMBRE> (editable por
    // empresa/período) con fallback a la tasa total del enum de dominio Afp (fuente
    // única de tasas oficiales SBS). Ver defaultAfpRate() y el enum Afp.
    // Fallback usado solo si el nombre de AFP no existe en el enum.
    private static final String AFP_RATE_DEFAULT_FALLBACK = "0.1290";

    /** Tasa AFP total por defecto (fuente única = enum Afp); fallback si el nombre es desconocido. */
    private static String defaultAfpRate(String afpNombre) {
        try {
            return com.microshop.rrhh.domain.enums.Afp.valueOf(afpNombre).tasaTotal().toPlainString();
        } catch (IllegalArgumentException e) {
            return AFP_RATE_DEFAULT_FALLBACK;
        }
    }

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollMapper payrollMapper;
    private final TenantContext tenantContext;
    private final MessageHelper msg;
    private final UsersParameterClient usersParameterClient;
    private final TesoreriaClient tesoreriaClient;
    private final ContabilidadClient contabilidadClient;

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
        aplicarAportesPrevisionales(payroll, employee, request.sueldoBase(),
                AppUtils.zeroIfNull(request.asignacionFamiliar()), AppUtils.zeroIfNull(request.montoHorasExtras()));
        Payroll saved = payrollRepository.save(payroll);

        log.info("Planilla creada: {} - Empleado: {} - Periodo: {} - Tenant: {}",
                saved.getId(), request.employeeId(), request.periodo(), tenantId);
        return payrollMapper.toDto(saved);
    }

    /**
     * Corrige una planilla existente (PUT /payroll/{id}). Solo permitido en estado GENERADO:
     * APROBADA ya disparó pago en tesorería + asiento contable (ver approvePayroll) y
     * PAGADA/CANCELADA son terminales — corregirlas dejaría esos efectos desincronizados
     * del monto real persistido. Recalcula AFP/ONP/EsSalud/Renta 5ta con el MISMO motor
     * que createPayroll/generatePayrollForPeriod (fuente única, ver hallazgo previsional).
     */
    public PayrollResponseDto updatePayroll(Long id, @Valid PayrollRequestDto request) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Payroll payroll = payrollRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException(msg.get("payroll.not.found")));

        if (payroll.getEstado() != Payroll.PayrollStatus.GENERADO) {
            throw new BusinessException(msg.get("payroll.estado.invalido", payroll.getEstado().name()));
        }

        Employee employee = payroll.getEmployee();
        if (employee == null || !employee.getId().equals(request.employeeId())) {
            employee = employeeRepository.findByIdAndTenantId(request.employeeId(), tenantId)
                    .orElseThrow(() -> new NotFoundException(msg.get("payroll.employee.not.found")));
        }

        boolean cambiaClave = !employee.getId().equals(payroll.getEmployee().getId())
                || !request.periodo().equals(payroll.getPeriodo());
        if (cambiaClave) {
            payrollRepository.findByTenantIdAndEmployee_IdAndPeriodo(tenantId, request.employeeId(), request.periodo())
                    .filter(p -> !p.getId().equals(id))
                    .ifPresent(p -> {
                        throw new ConflictException(msg.get("payroll.duplicate", request.employeeId(), request.periodo()));
                    });
        }

        payroll.setEmployee(employee);
        payrollMapper.updateEntity(payroll, request);

        // Limpia el detalle previo antes de regenerarlo: `details` tiene orphanRemoval=true
        // (ver Payroll.java), así que Hibernate emite los DELETE de las filas huérfanas dentro
        // del mismo flush en el que se insertan las nuevas — mismo patrón ya usado en
        // EvaluationCommandService.updateEvaluation. No hay unique constraint sobre
        // (payroll_id, concepto) en PayrollDetail, así que no aplica el gotcha de INSERT-antes-
        // que-DELETE de un derived delete (`deleteByXxx`) de Spring Data.
        payroll.getDetails().clear();
        aplicarAportesPrevisionales(payroll, employee, request.sueldoBase(),
                AppUtils.zeroIfNull(request.asignacionFamiliar()), AppUtils.zeroIfNull(request.montoHorasExtras()));

        Payroll updated = payrollRepository.save(payroll);
        log.info("Planilla corregida: {} - Empleado: {} - Periodo: {} - Tenant: {}",
                id, request.employeeId(), request.periodo(), tenantId);
        return payrollMapper.toDto(updated);
    }

    public List<PayrollResponseDto> generatePayrollForPeriod(String periodo) {
        Long tenantId = tenantContext.getCurrentTenantId();
        YearMonth ym = YearMonth.parse(periodo);
        int month = ym.getMonthValue();

        List<Employee> activeEmployees = employeeRepository.findByTenantIdAndEstado(
                tenantId, Employee.EmployeeStatus.ACTIVO);

        // Parámetros ERP: mismos valores para TODOS los empleados de la corrida — se izan
        // fuera del loop (antes: 5 HTTP bloqueantes POR empleado en calcularPlanillaPeruana).
        ParametrosPlanilla parametros = obtenerParametrosPlanilla();

        // Dedup: UNA query para el periodo completo en vez de un findBy...Periodo POR empleado (N+1).
        Set<Long> employeeIdsConPlanilla = payrollRepository.findByTenantIdAndPeriodo(tenantId, periodo).stream()
                .map(p -> p.getEmployee().getId())
                .collect(Collectors.toSet());

        List<Payroll> payrolls = activeEmployees.stream()
                .filter(emp -> !employeeIdsConPlanilla.contains(emp.getId()))
                .map(emp -> calcularPlanillaPeruana(emp, periodo, ym, month, parametros))
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

        // Sprint 7.2 — Disparar pago en tesorería (fire-and-forget no bloquea aprobación
        // si tesorería está caída; reintento manual via markAsPaid).
        try {
            BigDecimal monto = AppUtils.zeroIfNull(updated.getNeto());
            String periodo = updated.getPeriodo();
            Long employeeId = AppUtils.idOrNull(updated.getEmployee(), e -> e.getId());

            if (employeeId != null && monto.signum() > 0) {
                tesoreriaClient.createPayrollPayment(tenantId, employeeId, periodo, monto)
                        .doOnSuccess(paymentId -> log.info(
                                "Pago planilla disparado en tesorería: paymentId={} payrollId={} monto={}",
                                paymentId, id, monto))
                        .doOnError(e -> log.warn(
                                "Tesorería no respondió a pago planilla {}: {} (reintenta vía markAsPaid)",
                                id, e.getMessage()))
                        .subscribe();
            } else {
                log.warn("Planilla {} aprobada sin disparar pago — employeeId o monto inválido", id);
            }
        } catch (Exception e) {
            log.warn("Fallo disparando pago automático para planilla {}: {}", id, e.getMessage());
        }

        // S12 — Asiento de provisión de planilla en contabilidad (best-effort, sin outbox).
        // Si contabilidad falla, la aprobación ya quedó persistida; el asiento se reintenta manualmente.
        try {
            contabilidadClient.registrarAsientoPlanilla(updated)
                    .doOnError(e -> log.warn(
                            "Asiento planilla no registrado en contabilidad para payrollId={}: {}",
                            id, e.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            log.warn("Fallo disparando asiento de planilla a contabilidad para payrollId={}: {}", id, e.getMessage());
        }

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
     * Parámetros ERP compartidos por toda una corrida de planilla (mismos valores para
     * todos los empleados del periodo) — se calculan UNA vez en generatePayrollForPeriod
     * en vez de una llamada HTTP por empleado.
     */
    private record ParametrosPlanilla(
            BigDecimal rmv,
            BigDecimal asignacionFam,
            BigDecimal uit,
            BigDecimal tasaOnp,
            BigDecimal tasaEssalud) {
    }

    /** Fuente única de los parámetros ERP que alimentan el motor de planilla (RMV, asignación
     * familiar, UIT, tasa ONP, tasa EsSalud). La usan generatePayrollForPeriod (una vez por
     * corrida), createPayroll y updatePayroll (una vez por boleta individual). */
    private ParametrosPlanilla obtenerParametrosPlanilla() {
        return new ParametrosPlanilla(
                usersParameterClient.getDecimal("RMV", "1025.00"),
                usersParameterClient.getDecimal("ASIGNACION_FAMILIAR", "102.50"),
                usersParameterClient.getDecimal("UIT_ANIO", "5150.00"),
                usersParameterClient.getDecimal("TASA_ONP", "0.13"),
                usersParameterClient.getDecimal("TASA_ESSALUD", "0.09"));
    }

    /** Resultado del cálculo previsional: sistema previsional (etiqueta), AFP/ONP, EsSalud y Renta 5ta. */
    private record AportesPrevisionales(
            String sistemaPrevisional,
            String afpOnp,
            BigDecimal montoAfpOnp,
            BigDecimal essalud,
            BigDecimal rentaQuinta) {
    }

    /**
     * Cálculo previsional peruano (AFP/ONP individual + EsSalud + Renta 5ta) — FUENTE ÚNICA
     * usada tanto por calcularPlanillaPeruana (motor de corrida por periodo) como por
     * createPayroll/updatePayroll (alta y corrección manual de boleta individual). Antes de
     * este fix, la boleta manual saltaba por completo este cálculo (afpOnp/montoAfpOnp/essalud/
     * rentaQuinta quedaban en 0 → neto legalmente incorrecto).
     *
     * @param baseComputable sueldoBase + asignación familiar + monto horas extra (base de AFP/EsSalud)
     * @param sueldoBaseParaRenta sueldo base puro (proyección anual de Renta 5ta, sin asig./horas extra)
     */
    private AportesPrevisionales calcularAportesPrevisionales(Employee emp, BigDecimal baseComputable,
                                                               BigDecimal sueldoBaseParaRenta,
                                                               ParametrosPlanilla parametros) {
        // Sistema previsional y AFP: si el empleado no los tiene seteados, se asume ONP
        // (sistema público por defecto) — evita NPE y es el fallback más conservador.
        String sistemaPension = emp.getSistemaPrevisional() != null ? emp.getSistemaPrevisional() : "ONP";
        BigDecimal montoAfpOnp;
        if ("AFP".equals(sistemaPension) && emp.getAfpNombre() != null) {
            String afpNombre = emp.getAfpNombre().toUpperCase();
            String paramKey = "AFP_RATE_" + afpNombre;
            String defaultRate = defaultAfpRate(afpNombre);
            BigDecimal afpRate = usersParameterClient.getDecimal(paramKey, defaultRate);
            montoAfpOnp = baseComputable.multiply(afpRate).setScale(AppConstants.Money.ESCALA, AppConstants.Money.REDONDEO);
        } else {
            // sistemaPrevisional == "AFP" pero sin afpNombre seteado: no hay tasa de AFP que
            // aplicar (comportamiento preexistente del motor) → cae a tasa ONP como fallback.
            montoAfpOnp = baseComputable.multiply(parametros.tasaOnp()).setScale(AppConstants.Money.ESCALA, AppConstants.Money.REDONDEO);
        }

        BigDecimal essalud = baseComputable.multiply(parametros.tasaEssalud()).setScale(AppConstants.Money.ESCALA, AppConstants.Money.REDONDEO);

        BigDecimal proyAnual = sueldoBaseParaRenta.multiply(BigDecimal.valueOf(AppConstants.Negocio.MESES_PROYECCION_ANUAL)); // 12 + 2 grat
        BigDecimal deduccion7UIT = parametros.uit().multiply(BigDecimal.valueOf(AppConstants.Negocio.UIT_DEDUCCION_RENTA5TA));
        BigDecimal rentaQuinta = calcularRenta5ta(proyAnual, deduccion7UIT, parametros.uit());

        String afpOnpLabel = sistemaPension + (emp.getAfpNombre() != null ? " - " + emp.getAfpNombre() : "");
        return new AportesPrevisionales(sistemaPension, afpOnpLabel, montoAfpOnp, essalud, rentaQuinta);
    }

    /**
     * Calcula y aplica sobre `payroll` los 4 campos previsionales, vía
     * {@link #calcularAportesPrevisionales}, y regenera sus líneas de detalle (PayrollDetail)
     * vía {@link #construirDetalles} — MISMA construcción que usa la corrida automática
     * (calcularPlanillaPeruana), así una boleta manual (createPayroll) o corregida
     * (updatePayroll) nunca queda con detalle desincronizado de su cabecera. gratificacion/cts/
     * horasExtras (conteo de horas, no el monto) no son editables desde el request manual: se
     * toman tal cual están en `payroll` (0 en alta nueva vía @Builder.Default; preservados sin
     * cambio si se está corrigiendo una boleta que originalmente vino de la corrida automática
     * — updatePayroll/PayrollMapper.updateEntity no los toca).
     */
    private void aplicarAportesPrevisionales(Payroll payroll, Employee employee, BigDecimal sueldoBase,
                                              BigDecimal asignacionFamiliar, BigDecimal montoHorasExtras) {
        ParametrosPlanilla parametros = obtenerParametrosPlanilla();
        BigDecimal baseComputable = sueldoBase.add(asignacionFamiliar).add(montoHorasExtras);
        AportesPrevisionales aportes = calcularAportesPrevisionales(employee, baseComputable, sueldoBase, parametros);
        payroll.setAfpOnp(aportes.afpOnp());
        payroll.setMontoAfpOnp(aportes.montoAfpOnp());
        payroll.setEssalud(aportes.essalud());
        payroll.setRentaQuinta(aportes.rentaQuinta());

        construirDetalles(payroll, sueldoBase, asignacionFamiliar, montoHorasExtras,
                payroll.getHorasExtras(), payroll.getGratificacion(), payroll.getCts(),
                aportes, parametros.tasaEssalud());
    }

    /**
     * Full Peruvian payroll calculation.
     * AFP individual per employee, complete Renta 5ta (5 brackets),
     * overtime from attendance, gratificacion (Jul/Dec), CTS (May/Nov).
     */
    private Payroll calcularPlanillaPeruana(Employee emp, String periodo, YearMonth ym, int month,
                                             ParametrosPlanilla parametros) {
        BigDecimal rmv = parametros.rmv();
        BigDecimal asignacionFam = parametros.asignacionFam();
        BigDecimal tasaEssalud = parametros.tasaEssalud();

        // 1. Sueldo base — prioridad: (a) Salary abierto más reciente, (b) sueldo del
        // contrato ACTIVO más reciente (reconcilia ambas fuentes de remuneración), (c) RMV.
        BigDecimal sueldoBase = emp.getSalaries().stream()
                .filter(s -> s.getFechaFin() == null)
                .max(Comparator.comparing(Salary::getFechaInicio))
                .map(Salary::getSalarioBase)
                .or(() -> emp.getContracts().stream()
                        .filter(c -> c.getEstado() == Contract.ContractStatus.ACTIVO)
                        .filter(c -> c.getSalarioBase() != null)
                        .max(Comparator.comparing(Contract::getFechaInicio))
                        .map(Contract::getSalarioBase))
                .orElse(rmv);

        // 2. Asignación familiar
        BigDecimal asigFamiliar = emp.getDependents().isEmpty() ? BigDecimal.ZERO : asignacionFam;

        // 3. Overtime from attendance
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<Attendance> monthAttendance = attendanceRepository.findByTenantIdAndEmployeeIdAndFechaBetween(
                emp.getTenantId(), emp.getId(), start, end);

        BigDecimal horasExtras = monthAttendance.stream()
                .map(a -> AppUtils.zeroIfNull(a.getHorasExtras()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int diasTrabajados = (int) monthAttendance.stream()
                .filter(a -> a.getTipoRegistro() == Attendance.AttendanceType.NORMAL ||
                             a.getTipoRegistro() == Attendance.AttendanceType.TARDANZA)
                .count();

        // Overtime: 25% surcharge for first 2h, 35% after (DL 854 art. 10)
        BigDecimal tarifaHora = sueldoBase.divide(AppConstants.Negocio.HORAS_MES_LEGAL, AppConstants.Money.ESCALA_RATIO, AppConstants.Money.REDONDEO);
        BigDecimal montoHorasExtras = horasExtras.multiply(tarifaHora)
                .multiply(AppConstants.Negocio.RECARGO_HORA_EXTRA)
                .setScale(AppConstants.Money.ESCALA, AppConstants.Money.REDONDEO);

        // 4. Base computable
        BigDecimal baseComputable = sueldoBase.add(asigFamiliar).add(montoHorasExtras);

        // 5-7. AFP/ONP + EsSalud + Renta 5ta — fuente única (ver calcularAportesPrevisionales),
        // compartida con createPayroll/updatePayroll para que la boleta manual NUNCA diverja
        // del motor de planilla automático.
        AportesPrevisionales aportes = calcularAportesPrevisionales(emp, baseComputable, sueldoBase, parametros);
        BigDecimal montoAfpOnp = aportes.montoAfpOnp();
        BigDecimal essalud = aportes.essalud();
        BigDecimal rentaQuinta = aportes.rentaQuinta();

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
                    .add(grat.divide(BigDecimal.valueOf(AppConstants.Negocio.DIVISOR_GRATIFICACION), AppConstants.Money.ESCALA, AppConstants.Money.REDONDEO))
                    .divide(new BigDecimal("2"), AppConstants.Money.ESCALA, AppConstants.Money.REDONDEO); // semestral
        }

        // Build payroll with details
        Payroll payroll = Payroll.builder()
                .tenantId(emp.getTenantId())
                .employee(emp)
                .periodo(periodo)
                .sueldoBase(sueldoBase)
                .asignacionFamiliar(asigFamiliar)
                .afpOnp(aportes.afpOnp())
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

        // Add detail lines — fuente única, ver construirDetalles (compartida con createPayroll/
        // updatePayroll vía aplicarAportesPrevisionales).
        construirDetalles(payroll, sueldoBase, asigFamiliar, montoHorasExtras, horasExtras,
                gratificacion, cts, aportes, tasaEssalud);

        return payroll;
    }

    /**
     * Construye y agrega las líneas de detalle (PayrollDetail) de una boleta — FUENTE ÚNICA
     * usada tanto por calcularPlanillaPeruana (corrida automática por periodo) como por
     * createPayroll/updatePayroll vía {@link #aplicarAportesPrevisionales} (alta y corrección
     * manual). Antes de este fix, la boleta manual nacía sin detalle (`details = []`) y
     * corregir la cabecera de una boleta existente no regeneraba sus líneas, dejando montos de
     * detalle desincronizados de la cabecera recién recalculada.
     *
     * @param horasExtras cantidad de horas extra trabajadas (conteo, no monto) — 0 en boleta
     *                     manual (no se captura por asistencia)
     */
    private void construirDetalles(Payroll payroll, BigDecimal sueldoBase, BigDecimal asigFamiliar,
                                    BigDecimal montoHorasExtras, BigDecimal horasExtras,
                                    BigDecimal gratificacion, BigDecimal cts,
                                    AportesPrevisionales aportes, BigDecimal tasaEssalud) {
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
        addDetail(payroll, aportes.sistemaPrevisional(), PayrollDetail.ConceptType.DESCUENTO, aportes.montoAfpOnp(), null, null);
        if (aportes.rentaQuinta().compareTo(BigDecimal.ZERO) > 0) {
            addDetail(payroll, "Renta 5ta Categoría", PayrollDetail.ConceptType.DESCUENTO, aportes.rentaQuinta(), null, null);
        }
        addDetail(payroll, "EsSalud", PayrollDetail.ConceptType.APORTE_EMPLEADOR, aportes.essalud(), null, tasaEssalud.multiply(new BigDecimal("100")));
        if (cts.compareTo(BigDecimal.ZERO) > 0) {
            addDetail(payroll, "CTS", PayrollDetail.ConceptType.APORTE_EMPLEADOR, cts, null, null);
        }
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

        return impuesto.divide(BigDecimal.valueOf(AppConstants.Negocio.MESES_ANIO), AppConstants.Money.ESCALA, AppConstants.Money.REDONDEO);
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
