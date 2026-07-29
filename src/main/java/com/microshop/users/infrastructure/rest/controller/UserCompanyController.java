package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.UserCompanyCommandService;
import com.microshop.users.application.query.UserCompanyQueryService;
import com.microshop.users.application.mapper.UserCompanyMapper;
import com.microshop.users.application.dto.UserCompanyResponseDto;
import com.microshop.users.config.security.RequiresTenantAccess;
import com.microshop.users.config.security.SecurityContextUtils;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
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
    @Operation(summary = "Obtener empresas de un usuario",
               description = "El propio usuario ve todas sus membresías (lo necesita el cambio de "
                       + "empresa). Para otro usuario hay que compartir empresa, y solo se devuelve "
                       + "la membresía de la empresa propia.")
    public ResponseEntity<List<UserCompanyResponseDto>> getUserCompanies(
            @PathVariable @NonNull Long userId) {
        // Hasta 2026-07-28 este endpoint no tenía ninguna restricción y caía en
        // anyRequest().authenticated(): con cualquier JWT se podía iterar userId y mapear toda la
        // plantilla de la plataforma con su companyId y sus roleIds. Verificado con
        // AislamientoMultiTenantTest, que fallaba antes de este cambio.
        Long yo = SecurityContextUtils.currentUserId();
        boolean esElPropio = yo != null && yo.equals(userId);
        Long scope = SecurityContextUtils.isSuperAdmin() ? null : SecurityContextUtils.currentCompanyId();

        List<UserCompanyEntity> membresias = userCompanyQueryService.getUserCompanies(userId);

        if (!esElPropio && scope != null) {
            membresias = membresias.stream()
                    .filter(uc -> uc.getCompany() != null && scope.equals(uc.getCompany().getId()))
                    .collect(Collectors.toList());
            // 404 y no 403: un 403 confirmaría a un tenant ajeno que el userId existe.
            if (membresias.isEmpty()) return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(membresias.stream()
                .map(userCompanyMapper::toDto)
                .collect(Collectors.toList()));
    }
}
