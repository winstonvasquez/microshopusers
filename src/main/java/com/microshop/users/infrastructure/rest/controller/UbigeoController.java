package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.dto.UbigeoOptionDto;
import com.microshop.users.application.query.UbigeoQueryService;
import com.microshop.users.shared.constants.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Ubigeo oficial del INEI para los selects encadenados de dirección.
 *
 * <p><b>Sin {@code @RequiresTenantAccess} y a propósito:</b> la división política del Perú no
 * pertenece a ninguna empresa. No hay {@code companyId} que validar aquí y anotarlo solo daría
 * la falsa impresión de que estos datos están particionados por tenant. Es de solo lectura: no
 * existe endpoint de escritura, la tabla la siembra la migración V42.
 *
 * <p>Es público (ver {@code SecurityConfig}) porque el storefront lo necesita en el checkout de
 * un comprador invitado, que por definición todavía no tiene sesión.
 */
@RestController
@RequestMapping(ApiPaths.UBIGEO)
@RequiredArgsConstructor
public class UbigeoController {

    private final UbigeoQueryService ubigeoQueryService;

    /** Los 25 departamentos, para el primer select de la cadena. */
    @GetMapping("/departamentos")
    public ResponseEntity<List<UbigeoOptionDto>> getDepartamentos() {
        return ResponseEntity.ok(ubigeoQueryService.getDepartamentos());
    }

    /**
     * Provincias del departamento indicado (código INEI de 2 dígitos). Un código con otra forma
     * se responde vacío SIN llegar al servicio: como el endpoint es público y el resultado se
     * cachea por código, aceptar cualquier texto dejaría llenar la caché desde fuera.
     */
    @GetMapping("/departamentos/{departamentoCodigo}/provincias")
    public ResponseEntity<List<UbigeoOptionDto>> getProvincias(
            @PathVariable String departamentoCodigo) {
        if (!departamentoCodigo.matches("\\d{2}")) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(ubigeoQueryService.getProvincias(departamentoCodigo));
    }

    /** Distritos de la provincia indicada (código INEI de 4 dígitos). */
    @GetMapping("/provincias/{provinciaCodigo}/distritos")
    public ResponseEntity<List<UbigeoOptionDto>> getDistritos(@PathVariable String provinciaCodigo) {
        if (!provinciaCodigo.matches("\\d{4}")) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(ubigeoQueryService.getDistritos(provinciaCodigo));
    }

}
