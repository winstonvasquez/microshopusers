package com.microshop.users.application.command;

import com.microshop.users.infrastructure.persistence.entity.CompanyThemeConfigEntity;
import com.microshop.users.infrastructure.persistence.entity.UserThemePreferenceEntity;
import com.microshop.users.infrastructure.persistence.repository.CompanyThemeConfigRepository;
import com.microshop.users.infrastructure.persistence.repository.UserThemePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Comandos de escritura para preferencias de tema por usuario y empresa.
 * Las consultas están en ThemePreferenceQueryService.
 */
@Service
@RequiredArgsConstructor
public class ThemePreferenceService {

    private final UserThemePreferenceRepository userThemeRepo;
    private final CompanyThemeConfigRepository  companyThemeRepo;

    /** Guarda o actualiza la preferencia de tema del usuario. */
    @Transactional
    public void saveUserPreference(Long userId, Long companyId, String module, String themeKey) {
        var existing = userThemeRepo.findByUserIdAndCompanyIdAndModule(userId, companyId, module);
        UserThemePreferenceEntity entity = existing.orElseGet(() ->
                UserThemePreferenceEntity.builder()
                        .userId(userId).companyId(companyId).module(module).themeKey(themeKey).build());
        entity.setThemeKey(themeKey);
        entity.setUpdatedAt(LocalDateTime.now());
        userThemeRepo.save(entity);
    }

    /** Guarda o actualiza el tema base de empresa (solo ADMIN). */
    @Transactional
    public void saveCompanyTheme(Long companyId, String module, String themeKey) {
        var existing = companyThemeRepo.findByCompanyIdAndModule(companyId, module);
        CompanyThemeConfigEntity entity = existing.orElseGet(() ->
                CompanyThemeConfigEntity.builder()
                        .companyId(companyId).module(module).themeKey(themeKey).build());
        entity.setThemeKey(themeKey);
        entity.setUpdatedAt(LocalDateTime.now());
        companyThemeRepo.save(entity);
    }

    /** Elimina la preferencia del usuario para un módulo (vuelve al tema de empresa). */
    @Transactional
    public void deleteUserPreference(Long userId, Long companyId, String module) {
        userThemeRepo.deleteByUserIdAndCompanyIdAndModule(userId, companyId, module);
    }
}
