package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.AuthCommandService;
import com.microshop.users.application.command.UserCommandService;
import com.microshop.users.application.query.UserQueryService;
import com.microshop.users.application.dto.ChangePasswordRequest;
import com.microshop.users.application.dto.LoginResponse;
import com.microshop.users.application.dto.UserRequestDto;
import com.microshop.users.application.dto.UserResponseDto;
import com.microshop.users.infrastructure.persistence.entity.PolicyRoleEntity;
import com.microshop.users.infrastructure.persistence.repository.PolicyRoleRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.shared.constants.ApiPaths;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.USERS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gestión del catálogo de usuarios del sistema")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final AuthCommandService authCommandService;
    private final UsuarioRepository usuarioRepository;
    private final PolicyRoleRepository policyRoleRepository;

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

    @PutMapping("/me/password")
    @Operation(summary = "Cambiar contraseña del usuario autenticado",
               description = "Verifica la contraseña actual y actualiza a la nueva. Requiere autenticación.")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequest request) {
        log.info("PUT /api/users/me/password - Cambiando contraseña para: {}", userDetails.getUsername());
        userCommandService.changePassword(userDetails.getUsername(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/companies")
    @Operation(summary = "Obtener empresas del usuario autenticado",
               description = "Lista las empresas a las que pertenece el usuario. Requiere autenticación.")
    public ResponseEntity<List<Map<String, Object>>> getMyCompanies(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/users/me/companies - Consultando empresas para: {}", userDetails.getUsername());
        return ResponseEntity.ok(authCommandService.getMyCompanies(userDetails.getUsername()));
    }

    @PostMapping("/me/companies/switch")
    @Operation(summary = "Cambiar empresa activa del usuario autenticado",
               description = "Genera un nuevo JWT con la empresa destino. Requiere autenticación.")
    public ResponseEntity<LoginResponse> switchCompany(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Long> body) {
        Long targetCompanyId = body.get("targetCompanyId");
        log.info("POST /api/users/me/companies/switch - Cambiando a empresa {} para: {}", targetCompanyId, userDetails.getUsername());
        LoginResponse response = authCommandService.switchCompany(userDetails.getUsername(), targetCompanyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/permissions")
    @Operation(summary = "Obtener permisos del usuario autenticado",
               description = "Devuelve el rol y las políticas asignadas. Requiere autenticación.")
    public ResponseEntity<Map<String, Object>> getMyPermissions(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("GET /api/users/me/permissions - Consultando permisos para: {}", userDetails.getUsername());

        var usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        var rol = usuario.getRol();
        boolean isStandardCustomer = "CUSTOMER".equalsIgnoreCase(rol.getNombre())
                || "USER".equalsIgnoreCase(rol.getNombre());

        List<PolicyRoleEntity> policyRoles = policyRoleRepository.findByRolIdWithPolicy(rol.getId());

        var permissions = policyRoles.stream()
                .map(pr -> Map.of(
                        "codigo", pr.getPolicy().getCodigo(),
                        "nombre", pr.getPolicy().getNombre(),
                        "efecto", pr.getPolicy().getEfecto()
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "rolNombre", rol.getNombre(),
                "rolDescripcion", rol.getDescripcion() != null ? rol.getDescripcion() : "",
                "isStandardCustomer", isStandardCustomer,
                "permissions", permissions
        ));
    }
}
