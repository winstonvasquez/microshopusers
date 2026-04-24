package com.microshop.rrhh.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfig {

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
    public VentasClient ventasClient(WebClient ventasWebClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(ventasWebClient))
                .build();
        return factory.createClient(VentasClient.class);
    }
}
