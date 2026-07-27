package com.microshop.users.application.command;

import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.RolEntity;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyRoleEntity;
import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import com.microshop.users.infrastructure.persistence.repository.CompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.RolRepository;
import com.microshop.users.infrastructure.persistence.repository.SaasSubscriptionRepository;
import com.microshop.users.infrastructure.persistence.repository.UserCompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.UserCompanyRoleRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserCompanyCommandService {

    private final UserCompanyRepository userCompanyRepository;
    private final UserCompanyRoleRepository userCompanyRoleRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompanyRepository companyRepository;
    private final RolRepository rolRepository;
    private final SaasSubscriptionRepository subscriptionRepository;
    private final MessageSource messageSource;

    public void addUserToCompany(@NonNull Long userId, @NonNull Long companyId, @NonNull Long roleId) {
        var user = findUser(userId);
        var company = findCompany(companyId);
        var role = findRole(roleId);

        var userCompany = getOrCreateUserCompany(user, company);
        ensureUserCompanyIsActive(userCompany, company);
        ensureRoleAssigned(userCompany, role);
    }

    private UsuarioEntity findUser(@NonNull Long userId) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("user.not.found.simple", null, LocaleContextHolder.getLocale())));
    }

    private CompanyEntity findCompany(@NonNull Long companyId) {
        // Lock de fila: serializa altas concurrentes de usuarios contra el cupo del plan (ver enforceUserQuota).
        return companyRepository.findByIdForUpdate(companyId)
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("company.not.found", null, LocaleContextHolder.getLocale())));
    }

    private RolEntity findRole(@NonNull Long roleId) {
        return rolRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("role.not.found", null, LocaleContextHolder.getLocale())));
    }

    private UserCompanyEntity getOrCreateUserCompany(UsuarioEntity user, CompanyEntity company) {
        var existingWrapper = userCompanyRepository.findByUsuarioIdAndCompanyId(user.getId(), company.getId());
        if (existingWrapper.isPresent()) {
            return existingWrapper.get();
        }

        enforceUserQuota(company);
        return userCompanyRepository.save(UserCompanyEntity.builder()
                .usuario(user)
                .company(company)
                .build());
    }

    private void ensureUserCompanyIsActive(UserCompanyEntity userCompany, CompanyEntity company) {
        if (!userCompany.isActive()) {
            enforceUserQuota(company);
            userCompany.setActive(true);
            userCompanyRepository.save(userCompany);
        }
    }

    /**
     * Valida el cupo de usuarios activos del plan SaaS de la empresa. Sin suscripción activa
     * no se restringe (mismo criterio de fallback permisivo que SaasQueryService.getEnabledModuleCodes).
     * Debe llamarse SOLO antes de crear una membresía nueva o reactivar una inactiva — nunca al
     * agregar un rol adicional a una membresía ya activa (eso no consume cupo).
     */
    private void enforceUserQuota(CompanyEntity company) {
        subscriptionRepository.findByCompanyId(company.getId()).ifPresent(subscription -> {
            int maxUsers = subscription.getPlan().getMaxUsers();
            long activeUsers = userCompanyRepository.countByCompanyIdAndIsActiveTrue(company.getId());
            if (activeUsers >= maxUsers) {
                throw new ConflictException(messageSource.getMessage("company.user.quota.exceeded",
                        new Object[]{maxUsers, subscription.getPlan().getName()}, LocaleContextHolder.getLocale()));
            }
        });
    }

    private void ensureRoleAssigned(UserCompanyEntity userCompany, RolEntity role) {
        boolean roleExists = userCompany.getRoles() != null && userCompany.getRoles().stream()
                .anyMatch(ucr -> {
                    RolEntity r = ucr.getRol();
                    return r != null && r.getId() != null && r.getId().equals(role.getId());
                });

        if (!roleExists) {
            UserCompanyRoleEntity userCompanyRole = UserCompanyRoleEntity.builder()
                    .userCompany(userCompany)
                    .rol(role)
                    .build();
            userCompanyRoleRepository.save(userCompanyRole);
        }
    }
}
