package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.query.RolQueryService;
import com.microshop.users.application.dto.RolResponseDto;
import com.microshop.users.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.ROLES)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Roles", description = "Consulta del catálogo de roles del sistema")
public class RolController {

    private final RolQueryService rolQueryService;

    @GetMapping
    @Operation(summary = "Listar todos los roles")
    public ResponseEntity<List<RolResponseDto>> getAllRoles() {
        log.info("GET /api/roles - Obteniendo todos los roles");
        List<RolResponseDto> roles = rolQueryService.findAll();
        return ResponseEntity.ok(roles);
    }
}
