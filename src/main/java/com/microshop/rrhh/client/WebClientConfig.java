package com.microshop.rrhh.client;

import java.time.Duration;

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
                    .header("X-Internal-Token", internalToken)
                    .build();
            return next.exchange(withHeader);
        };
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .responseTimeout(RESPONSE_TIMEOUT);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
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
