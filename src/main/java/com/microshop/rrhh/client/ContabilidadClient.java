package com.microshop.rrhh.client;

import com.microshop.rrhh.domain.model.Payroll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cliente s2s hacia microshopcontabilidad para registrar el asiento de provisión
 * de planilla al momento de aprobar una nómina.
 *
 * <p>Endpoint receptor: {@code POST /finance/api/v1/contabilidad/automatico/planilla}
 * (autenticado vía X-Internal-Token — inyectado por el filtro del bean contabilidadWebClient).</p>
 */
@Component
@Slf4j
public class ContabilidadClient {

    private final WebClient webClient;

    public ContabilidadClient(@Qualifier("contabilidadWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Notifica a contabilidad para generar el asiento de provisión de planilla.
     *
     * <p>Cálculo de totalIngresos: sueldoBase + bonos + montoHorasExtras + asignacionFamiliar
     * (igual que {@code Payroll#calculateNeto()} antes de descuentos).</p>
     *
     * @param payroll planilla ya aprobada y persistida
     * @return Mono vacío — se suscribe fire-and-forget en el caller
     */
    public Mono<Void> registrarAsientoPlanilla(Payroll payroll) {
        BigDecimal totalIngresos = sum(payroll.getSueldoBase(),
                payroll.getBonos(),
                payroll.getMontoHorasExtras(),
                payroll.getAsignacionFamiliar());

        Map<String, Object> body = new LinkedHashMap<>();
        // companyId viaja como UUID sintético new UUID(0, tenantId) — el DTO receptor lo espera
        // como UUID (convención del proyecto, igual que JwtAuthenticationFilter.parseUuidOrLong).
        // Enviar el Long crudo causaba 400 al deserializar a UUID en contabilidad.
        body.put("companyId",     new java.util.UUID(0L, payroll.getTenantId()).toString());
        body.put("payrollId",     payroll.getId());
        body.put("periodo",       payroll.getPeriodo());
        body.put("fecha",         LocalDate.now().toString());
        body.put("empleadoId",    payroll.getEmployee() != null ? payroll.getEmployee().getId() : null);
        body.put("totalIngresos", totalIngresos);
        body.put("essalud",       nvl(payroll.getEssalud()));
        body.put("neto",          nvl(payroll.getNeto()));
        body.put("rentaQuinta",   nvl(payroll.getRentaQuinta()));
        body.put("descuentos",    nvl(payroll.getDescuentos()));
        body.put("aporteAfpOnp",  nvl(payroll.getMontoAfpOnp()));

        return webClient.post()
                .uri("/finance/api/v1/contabilidad/automatico/planilla")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info(
                        "Asiento planilla disparado a contabilidad: payrollId={} periodo={} tenant={}",
                        payroll.getId(), payroll.getPeriodo(), payroll.getTenantId()))
                .doOnError(e -> log.warn(
                        "Contabilidad no respondió al asiento de planilla payrollId={}: {} " +
                        "(best-effort — sin outbox en users; reintento manual pendiente)",
                        payroll.getId(), e.getMessage()));
    }

    private static BigDecimal sum(BigDecimal... values) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            total = total.add(v != null ? v : BigDecimal.ZERO);
        }
        return total;
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
