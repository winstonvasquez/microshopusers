package com.microshop.rrhh.client;

import java.time.Duration;

import com.microshop.users.shared.constants.AppConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    private static final int CONNECT_TIMEOUT_MS = 2_000;
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(5);

    @Value("${microshop.internal.token:}")
    private String internalToken;

    private ExchangeFilterFunction internalTokenFilter() {
        return (req, next) -> {
            if (!StringUtils.hasText(internalToken)) {
                return next.exchange(req);
            }
            ClientRequest withHeader = ClientRequest.from(req)
                    .header(AppConstants.Seguridad.X_INTERNAL_TOKEN, internalToken)
                    .build();
            return next.exchange(withHeader);
        };
    }

    private ExchangeFilterFunction correlationIdFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            String cid = org.slf4j.MDC.get("correlationId");
            return reactor.core.publisher.Mono.just(cid == null ? request
                    : ClientRequest.from(request)
                            .header("X-Correlation-Id", cid)
                            .build());
        });
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .responseTimeout(RESPONSE_TIMEOUT);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(correlationIdFilter());
    }

    @Bean
    public WebClient ventasWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8081")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient tesoreriaWebClient(WebClient.Builder builder) {
        // Fix S2S 2026-07-20: el pago de planilla (/treasury/api/payments/payroll) fallaba
        // silenciosamente con 401 — este bean no adjuntaba X-Internal-Token (a diferencia
        // de contabilidadWebClient). Mismo patrón s2s del 2026-04-28.
        return builder.clone()
                .baseUrl("http://localhost:8084")
                .defaultHeader("Content-Type", "application/json")
                .filter(internalTokenFilter())
                .build();
    }

    @Bean
    public WebClient contabilidadWebClient(WebClient.Builder builder) {
        return builder.clone()
                .baseUrl("http://localhost:8084")
                .defaultHeader("Content-Type", "application/json")
                .filter(internalTokenFilter())
                .build();
    }

    @Bean
    public VentasClient ventasClient(WebClient ventasWebClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(ventasWebClient))
                .build();
        return factory.createClient(VentasClient.class);
    }
}
