package com.microshop.rrhh.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfig {

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
        return WebClient.builder();
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
        return builder
                .baseUrl("http://localhost:8084")
                .defaultHeader("Content-Type", "application/json")
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
