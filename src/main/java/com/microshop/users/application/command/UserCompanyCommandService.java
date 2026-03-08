package com.microshop.users.application.command;

import com.microshop.users.infrastructure.persistence.entity.CompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.RolEntity;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyEntity;
import com.microshop.users.infrastructure.persistence.entity.UserCompanyRoleEntity;
import com.microshop.users.infrastructure.persistence.entity.UsuarioEntity;
import com.microshop.users.infrastructure.persistence.repository.CompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.RolRepository;
import com.microshop.users.infrastructure.persistence.repository.UserCompanyRepository;
import com.microshop.users.infrastructure.persistence.repository.UserCompanyRoleRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void addUserToCompany(@NonNull Long userId, @NonNull Long companyId, @NonNull Long roleId) {
        var user = findUser(userId);
        var company = findCompany(companyId);
        var role = findRole(roleId);

        var userCompany = getOrCreateUserCompany(user, company);
        ensureUserCompanyIsActive(userCompany);
        ensureRoleAssigned(userCompany, role);
    }

    private UsuarioEntity findUser(@NonNull Long userId) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private CompanyEntity findCompany(@NonNull Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
    }

    private RolEntity findRole(@NonNull Long roleId) {
        return rolRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
    }

    private UserCompanyEntity getOrCreateUserCompany(UsuarioEntity user, CompanyEntity company) {
        var existingWrapper = userCompanyRepository.findByUsuarioIdAndCompanyId(user.getId(), company.getId());
        if (existingWrapper.isPresent()) {
            return existingWrapper.get();
        }

        return userCompanyRepository.save(UserCompanyEntity.builder()
                .usuario(user)
                .company(company)
                .build());
    }

    private void ensureUserCompanyIsActive(UserCompanyEntity userCompany) {
        if (!userCompany.isActive()) {
            userCompany.setActive(true);
            userCompanyRepository.save(userCompany);
        }
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
