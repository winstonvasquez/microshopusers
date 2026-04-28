package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.UserCompanyCommandService;
import com.microshop.users.application.query.UserCompanyQueryService;
import com.microshop.users.application.mapper.UserCompanyMapper;
import com.microshop.users.application.dto.UserCompanyResponseDto;
import com.microshop.users.config.security.RequiresTenantAccess;
import com.microshop.users.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiPaths.USER_COMPANIES)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuario-Empresa", description = "Gestión de la relación entre usuarios y empresas")
public class UserCompanyController {

    private final UserCompanyCommandService userCompanyCommandService;
    private final UserCompanyQueryService userCompanyQueryService;
    private final UserCompanyMapper userCompanyMapper;

    @PostMapping("/assign")
    @Operation(summary = "Asignar usuario a empresa con rol (admin tenant o SUPERADMIN)")
    @RequiresTenantAccess(allowSuperAdmin = true)
    public ResponseEntity<Void> assignUserToCompany(@RequestParam @NonNull Long userId,
            @RequestParam @NonNull Long companyId,
            @RequestParam @NonNull Long roleId) {
        userCompanyCommandService.addUserToCompany(userId, companyId, roleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener empresas de un usuario")
    public ResponseEntity<List<UserCompanyResponseDto>> getUserCompanies(
            @PathVariable @NonNull Long userId) {
        List<UserCompanyResponseDto> userCompanies = userCompanyQueryService.getUserCompanies(userId)
                .stream()
                .map(userCompanyMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userCompanies);
    }
}
