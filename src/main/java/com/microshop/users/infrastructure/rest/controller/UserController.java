package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.AuthCommandService;
import com.microshop.users.application.command.UserCommandService;
import com.microshop.users.application.query.UserQueryService;
import com.microshop.users.application.dto.ChangePasswordRequest;
import com.microshop.users.application.dto.ChangeRoleRequest;
import com.microshop.users.application.dto.LoginResponse;
import com.microshop.users.application.dto.UserExportRowDto;
import com.microshop.users.application.dto.UserRequestDto;
import com.microshop.users.application.dto.UserResponseDto;
import com.microshop.users.config.security.SecurityContextUtils;
import com.microshop.users.shared.constants.ApiPaths;
import com.microshop.users.shared.util.SpreadsheetExporter;

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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiPaths.USERS)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gestión del catálogo de usuarios del sistema")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final AuthCommandService authCommandService;

    /**
     * Resuelve el companyId a usar como scope de tenant: {@code null} SOLO para un SUPERADMIN
     * (bypass intencional, sin acotar). Un usuario normal SIN companyId resoluble en el JWT
     * es rechazado explícitamente — nunca cae a "sin acotar" por accidente (fail-closed, no fail-open).
     */
    private Long resolveTenantScope() {
        if (SecurityContextUtils.isSuperAdmin()) return null;
        Long companyId = SecurityContextUtils.currentCompanyId();
        if (companyId == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "JWT sin claim companyId — no se puede acotar por tenant");
        }
        return companyId;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios paginados")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) Long rolId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCreacionDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCreacionHasta) {
        log.info("GET /api/users - Obteniendo usuarios con paginación");
        // LocalDate (yyyy-MM-dd, lo que envía el date-range del frontend) -> Instant día completo.
        Instant desde = fechaCreacionDesde != null ? fechaCreacionDesde.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant hasta = fechaCreacionHasta != null ? fechaCreacionHasta.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant() : null;
        Page<UserResponseDto> users = userQueryService.findAll(pageable, resolveTenantScope(), rolId, desde, hasta);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los usuarios sin paginación")
    public ResponseEntity<List<UserResponseDto>> getAllUsersNoPagination() {
        log.info("GET /api/users/all - Obteniendo todos los usuarios");
        List<UserResponseDto> users = userQueryService.findAll(resolveTenantScope());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/report/export")
    @Operation(summary = "Exportar reporte de clientes/usuarios (admin/reportes) a XLSX o CSV — mismas columnas que la vista")
    public ResponseEntity<byte[]> exportReporteClientes(@RequestParam(defaultValue = "xlsx") String format) {
        // Replica exactamente lo que arma admin/pages/reportes/reportes-clientes.component.ts,
        // acotado a la empresa del caller salvo que sea SUPERADMIN (ver getAllUsersNoPagination()).
        List<UserExportRowDto> usuarios = userQueryService.findAllForExport(resolveTenantScope());

        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

        List<String> cabeceras = List.of("ID", "Usuario", "Email", "Nombre", "Apellido", "Estado", "Fecha Registro");
        List<List<Object>> filas = usuarios.stream()
                .map(u -> List.<Object>of(
                        u.id(),
                        valorOVacio(u.username()),
                        valorOVacio(u.email()),
                        valorOVacio(u.nombres()),
                        valorOVacio(u.apellidos()),
                        u.activo() ? "ACTIVO" : "INACTIVO",
                        u.fechaCreacion() != null ? fechaFormatter.format(u.fechaCreacion()) : ""))
                .collect(Collectors.toList());

        byte[] bytes;
        String filename;
        MediaType contentType;
        if ("csv".equalsIgnoreCase(format)) {
            bytes = SpreadsheetExporter.toCsv(cabeceras, filas);
            filename = "reporte-clientes.csv";
            contentType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        } else {
            bytes = SpreadsheetExporter.toXlsx("Reporte de Clientes", cabeceras, filas);
            filename = "reporte-clientes.xlsx";
            contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(contentType)
                .body(bytes);
    }

    /** Retorna cadena vacía si el valor es null, para no propagar "null" literal a la exportación. */
    private static String valorOVacio(String valor) {
        return valor != null ? valor : "";
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

    @PutMapping("/{id}/role")
    @Operation(summary = "Cambiar el rol de un usuario", description = "Restringido a SUPERADMIN — permite otorgar/revocar SUPERADMIN u otros roles cross-tenant.")
    public ResponseEntity<UserResponseDto> changeRole(
            @PathVariable @NonNull Long id,
            @RequestBody @Valid ChangeRoleRequest request) {
        log.info("PUT /api/users/{}/role - Cambiando rol a {}", id, request.roleCode());
        UserResponseDto updated = userCommandService.changeRole(id, request.roleCode());
        return ResponseEntity.ok(updated);
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
        return ResponseEntity.ok(userQueryService.getMyPermissions(userDetails.getUsername()));
    }
}
