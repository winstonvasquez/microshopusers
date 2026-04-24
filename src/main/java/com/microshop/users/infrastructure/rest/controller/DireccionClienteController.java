package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.infrastructure.persistence.entity.DireccionClienteEntity;
import com.microshop.users.infrastructure.persistence.repository.DireccionClienteRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de direcciones de envío del cliente.
 * Todas las operaciones aplican al usuario autenticado (extraído del JWT).
 */
@RestController
@RequestMapping(ApiPaths.CLIENTE_DIRECCIONES)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Direcciones Cliente", description = "CRUD de direcciones de envío del cliente autenticado")
public class DireccionClienteController {

    private final DireccionClienteRepository direccionRepo;
    private final UsuarioRepository usuarioRepo;

    // DTO de entrada para crear/actualizar una dirección
    public record DireccionRequest(
            String nombreCompleto,
            String telefono,
            String departamento,
            String provincia,
            String distrito,
            String direccionLinea1,
            String referencia,
            boolean esPrincipal) {
    }

    // DTO de salida
    public record DireccionResponse(
            Long id,
            String nombreCompleto,
            String telefono,
            String departamento,
            String provincia,
            String distrito,
            String direccionLinea1,
            String referencia,
            boolean esPrincipal) {
    }

    /** Obtiene el ID del usuario autenticado a partir de su username en el SecurityContext. */
    private Long resolveUserId(UserDetails userDetails) {
        return usuarioRepo.findByUsername(userDetails.getUsername())
                .or(() -> usuarioRepo.findByEmail(userDetails.getUsername()))
                .map(u -> u.getId())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado: " + userDetails.getUsername()));
    }

    private DireccionResponse toResponse(DireccionClienteEntity e) {
        return new DireccionResponse(
                e.getId(),
                e.getNombreCompleto(),
                e.getTelefono(),
                e.getDepartamento(),
                e.getProvincia(),
                e.getDistrito(),
                e.getDireccionLinea1(),
                e.getReferencia(),
                e.isEsPrincipal());
    }

    @GetMapping
    @Operation(summary = "Listar direcciones del cliente autenticado")
    public ResponseEntity<List<DireccionResponse>> listar(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        log.debug("GET /api/clientes/me/direcciones - userId={}", userId);
        List<DireccionResponse> list = direccionRepo.findByUserIdAndActivoTrue(userId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @Operation(summary = "Agregar nueva dirección al cliente autenticado")
    public ResponseEntity<DireccionResponse> crear(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DireccionRequest request) {
        Long userId = resolveUserId(userDetails);
        log.debug("POST /api/clientes/me/direcciones - userId={}", userId);

        // Si se marca como principal, desmarcar las anteriores
        if (request.esPrincipal()) {
            limpiarPrincipal(userId);
        }

        DireccionClienteEntity nueva = DireccionClienteEntity.builder()
                .userId(userId)
                .nombreCompleto(request.nombreCompleto())
                .telefono(request.telefono())
                .departamento(request.departamento())
                .provincia(request.provincia())
                .distrito(request.distrito())
                .direccionLinea1(request.direccionLinea1())
                .referencia(request.referencia())
                .esPrincipal(request.esPrincipal())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(direccionRepo.save(nueva)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar dirección del cliente autenticado")
    public ResponseEntity<DireccionResponse> actualizar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody DireccionRequest request) {
        Long userId = resolveUserId(userDetails);
        log.debug("PUT /api/clientes/me/direcciones/{} - userId={}", id, userId);

        DireccionClienteEntity existente = direccionRepo.findById(id)
                .filter(d -> d.getUserId().equals(userId) && d.isActivo())
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada: " + id));

        if (request.esPrincipal() && !existente.isEsPrincipal()) {
            limpiarPrincipal(userId);
        }

        existente.setNombreCompleto(request.nombreCompleto());
        existente.setTelefono(request.telefono());
        existente.setDepartamento(request.departamento());
        existente.setProvincia(request.provincia());
        existente.setDistrito(request.distrito());
        existente.setDireccionLinea1(request.direccionLinea1());
        existente.setReferencia(request.referencia());
        existente.setEsPrincipal(request.esPrincipal());

        return ResponseEntity.ok(toResponse(direccionRepo.save(existente)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar dirección del cliente autenticado (soft delete)")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = resolveUserId(userDetails);
        log.debug("DELETE /api/clientes/me/direcciones/{} - userId={}", id, userId);

        DireccionClienteEntity existente = direccionRepo.findById(id)
                .filter(d -> d.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada: " + id));

        existente.setActivo(false);
        existente.setEsPrincipal(false);
        direccionRepo.save(existente);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/principal")
    @Operation(summary = "Marcar dirección como principal del cliente autenticado")
    public ResponseEntity<DireccionResponse> marcarPrincipal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = resolveUserId(userDetails);
        log.debug("PUT /api/clientes/me/direcciones/{}/principal - userId={}", id, userId);

        limpiarPrincipal(userId);

        DireccionClienteEntity existente = direccionRepo.findById(id)
                .filter(d -> d.getUserId().equals(userId) && d.isActivo())
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada: " + id));

        existente.setEsPrincipal(true);
        return ResponseEntity.ok(toResponse(direccionRepo.save(existente)));
    }

    /** Desmarca la dirección principal actual del usuario. */
    private void limpiarPrincipal(Long userId) {
        direccionRepo.findByUserIdAndEsPrincipalTrueAndActivoTrue(userId)
                .ifPresent(d -> {
                    d.setEsPrincipal(false);
                    direccionRepo.save(d);
                });
    }
}
