package com.microshop.rrhh.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@HttpExchange(url = "/sales")
public interface VentasClient {

    @PostExchange("/api/inventario/actualizar")
    Mono<Boolean> actualizarInventario(Map<String, Object> data);

    @GetExchange("/api/productos/validar-existencia")
    Mono<Boolean> validarExistenciaProductos(Map<String, Object> data);
}
