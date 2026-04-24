package com.microshop.rrhh.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
public class TesoreriaClient {

    private final WebClient webClient;

    public TesoreriaClient(@org.springframework.beans.factory.annotation.Qualifier("tesoreriaWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Long> createPayrollPayment(Long tenantId, Long employeeId, String periodo, BigDecimal amount) {
        Map<String, Object> paymentRequest = Map.of(
                "tenantId", tenantId,
                "employeeId", employeeId,
                "periodo", periodo,
                "amount", amount,
                "type", "PAYROLL"
        );

        return webClient.post()
                .uri("/treasury/api/payments/payroll")
                .bodyValue(paymentRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> ((Number) response.get("id")).longValue())
                .doOnSuccess(id -> log.info("Pago de planilla creado en tesorería: {} - Tenant: {}", id, tenantId))
                .doOnError(error -> log.error("Error al crear pago de planilla en tesorería", error));
    }
}
