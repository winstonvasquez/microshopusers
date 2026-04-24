package com.microshop.users.application.dto;

import java.time.LocalDateTime;

public record ErrorDto(
    String codigo,
    String mensaje,
    LocalDateTime timestamp
) {
    public ErrorDto(String codigo, String mensaje) {
        this(codigo, mensaje, LocalDateTime.now());
    }
}
