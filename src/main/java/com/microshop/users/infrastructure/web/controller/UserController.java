package com.microshop.users.infrastructure.web.controller;

import com.microshop.users.application.command.UserCommandService;
import com.microshop.users.application.query.UserQueryService;
import com.microshop.users.infrastructure.web.dto.UserRequestDto;
import com.microshop.users.infrastructure.web.dto.UserResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gestión del catálogo de usuarios del sistema")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Error de validación"),
    @ApiResponse(responseCode = "404", description = "No encontrado"),
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
})
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @GetMapping
    @Operation(summary = "Listar usuarios paginados")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/users - Obteniendo usuarios con paginación");
        Page<UserResponseDto> users = userQueryService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los usuarios sin paginación")
    public ResponseEntity<List<UserResponseDto>> getAllUsersNoPagination() {
        log.info("GET /api/users/all - Obteniendo todos los usuarios");
        List<UserResponseDto> users = userQueryService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable @NonNull Long id) {
        log.info("GET /api/users/{} - Obteniendo usuario por ID", id);
        return userQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Obtener usuario por username")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable @NonNull String username) {
        log.info("GET /api/users/username/{} - Obteniendo usuario por username", username);
        return userQueryService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto userDto) {
        log.info("POST /api/users - Creando nuevo usuario: {}", userDto.username());
        UserResponseDto created = userCommandService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario existente")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable @NonNull Long id,
            @RequestBody @Valid UserRequestDto userDto) {
        log.info("PUT /api/users/{} - Actualizando usuario", id);
        UserResponseDto updated = userCommandService.updateUser(id, userDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar usuario (soft delete)")
    public ResponseEntity<Void> deleteUser(@PathVariable @NonNull Long id) {
        log.info("DELETE /api/users/{} - Eliminando usuario", id);
        userCommandService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-rol/{rolId}")
    @Operation(summary = "Obtener usuarios por rol")
    public ResponseEntity<List<UserResponseDto>> getUsersByRol(@PathVariable @NonNull Long rolId) {
        log.info("GET /api/users/by-rol/{} - Obteniendo usuarios por rol", rolId);
        List<UserResponseDto> users = userQueryService.findByRol(rolId);
        return ResponseEntity.ok(users);
    }
}
