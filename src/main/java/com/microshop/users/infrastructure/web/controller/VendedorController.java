package com.microshop.users.infrastructure.web.controller;

import com.microshop.users.application.command.VendedorCommandService;
import com.microshop.users.application.query.VendedorQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.microshop.users.infrastructure.web.dto.VendedorRequestDto;
import com.microshop.users.infrastructure.web.dto.VendedorResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendedores")
@RequiredArgsConstructor
@Tag(name = "Vendedores", description = "Endpoints para la gestión de perfiles de vendedores")
public class VendedorController {

    private final VendedorCommandService vendedorCommandService;
    private final VendedorQueryService vendedorQueryService;

    @PostMapping("/users/{usuarioId}")
    @Operation(summary = "Registrar un usuario como vendedor", description = "Crea un perfil de vendedor para un usuario existente")
    public ResponseEntity<VendedorResponseDto> registerSeller(
            @PathVariable Long usuarioId,
            @Valid @RequestBody VendedorRequestDto request) {
        return new ResponseEntity<>(vendedorCommandService.registerSeller(usuarioId, request), HttpStatus.CREATED);
    }

    @GetMapping("/users/{usuarioId}")
    @Operation(summary = "Obtener perfil de vendedor por ID de usuario", description = "Retorna el perfil de vendedor asociado a un usuario")
    public ResponseEntity<VendedorResponseDto> getSellerByUsuarioId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(vendedorQueryService.getSellerByUsuarioId(usuarioId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener perfil de vendedor por su ID", description = "Retorna el perfil de vendedor")
    public ResponseEntity<VendedorResponseDto> getSellerById(@PathVariable Long id) {
        return ResponseEntity.ok(vendedorQueryService.getSellerById(id));
    }

    @GetMapping
    @Operation(summary = "Obtener todos los vendedores", description = "Retorna la lista de todos los perfiles de vendedor")
    public ResponseEntity<List<VendedorResponseDto>> getAllSellers() {
        return ResponseEntity.ok(vendedorQueryService.getAllSellers());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de aprobación del vendedor", description = "Actualiza el estado (PENDING, APPROVED, REJECTED)")
    public ResponseEntity<VendedorResponseDto> updateSellerStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(vendedorCommandService.updateSellerStatus(id, status));
    }
}
