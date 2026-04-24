package com.microshop.rrhh.infrastructure.rest.controller;

import com.microshop.rrhh.application.dto.configuracion.ConfiguracionRemunerativaDto;
import com.microshop.rrhh.client.UsersParameterClient;
import com.microshop.rrhh.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Devuelve los parámetros remunerativos vigentes (UIT, tasas, tramos Renta 5ta).
 * Los valores dinámicos (UIT, tasas) se obtienen del servicio centralizado de parámetros.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.CONFIGURACION)
@Tag(name = "Configuración Remunerativa", description = "Parámetros tributarios y laborales vigentes — SUNAT")
public class ConfiguracionController {

    private final UsersParameterClient usersParameterClient;

    @GetMapping("/remunerativa")
    @Operation(
        summary = "Obtener parámetros remunerativos vigentes",
        description = "Retorna UIT vigente, tasas AFP (comisión flujo), ONP, ESSALUD y tramos Renta 5ta Categoría"
    )
    public ResponseEntity<ConfiguracionRemunerativaDto> getConfiguracionRemunerativa() {
        double uit = usersParameterClient.getDecimal("UIT_ANIO", "5150.00").doubleValue();
        double essalud = usersParameterClient.getDecimal("TASA_ESSALUD", "0.09").doubleValue();
        double onp = usersParameterClient.getDecimal("TASA_ONP", "0.13").doubleValue();
        double asigFamiliar = usersParameterClient.getDecimal("ASIGNACION_FAMILIAR", "102.50").doubleValue();
        int anio = LocalDate.now().getYear();

        var config = new ConfiguracionRemunerativaDto(
            anio,
            uit,
            essalud,
            onp,
            asigFamiliar,
            List.of(
                new ConfiguracionRemunerativaDto.TramoRenta5ta(0,  5,              0.08),
                new ConfiguracionRemunerativaDto.TramoRenta5ta(5,  20,             0.14),
                new ConfiguracionRemunerativaDto.TramoRenta5ta(20, 35,             0.17),
                new ConfiguracionRemunerativaDto.TramoRenta5ta(35, 45,             0.20),
                new ConfiguracionRemunerativaDto.TramoRenta5ta(45, Double.MAX_VALUE, 0.30)
            ),
            new ConfiguracionRemunerativaDto.AfpTasas(0.10, 0.01748, 0.00874),  // INTEGRA
            new ConfiguracionRemunerativaDto.AfpTasas(0.10, 0.01842, 0.01069),  // PRIMA
            new ConfiguracionRemunerativaDto.AfpTasas(0.10, 0.01842, 0.01587),  // PROFUTURO
            new ConfiguracionRemunerativaDto.AfpTasas(0.10, 0.01842, 0.00773)   // HABITAT
        );
        return ResponseEntity.ok(config);
    }
}
