package com.microshop.users.application.query;

import com.microshop.users.application.MessageHelper;
import com.microshop.users.infrastructure.persistence.repository.CompanyThemeConfigRepository;
import com.microshop.users.infrastructure.persistence.repository.UserThemePreferenceRepository;
import com.microshop.users.infrastructure.persistence.repository.UsuarioRepository;
import com.microshop.users.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultas de preferencias de tema a nivel de empresa y de usuario individual.
 * Resolución: usuario > empresa > global (erp_parameters).
 */
@Service
@RequiredArgsConstructor
public class ThemePreferenceQueryService {

    private final UserThemePreferenceRepository userThemeRepo;
    private final CompanyThemeConfigRepository  companyThemeRepo;
    private final UsuarioRepository             usuarioRepo;
    private final MessageHelper                 msg;

    /**
     * Resuelve el tema efectivo para el usuario y módulo dados.
     * Cadena de resolución: preferencia de usuario > tema base de empresa > null.
     * El llamador usa null como señal para caer al tema global de erp_parameters.
     */
    @Transactional(readOnly = true)
    public String resolveTheme(Long userId, Long companyId, String module) {
        var userPref = userThemeRepo.findByUserIdAndCompanyIdAndModule(userId, companyId, module);
        if (userPref.isPresent()) return userPref.get().getThemeKey();

        var companyPref = companyThemeRepo.findByCompanyIdAndModule(companyId, module);
        if (companyPref.isPresent()) return companyPref.get().getThemeKey();

        return null;
    }

    /** Resuelve el userId desde el username del token JWT. */
    @Transactional(readOnly = true)
    public Long resolveUserId(String username) {
        return usuarioRepo.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new NotFoundException(msg.get("theme.user.not.found", username)));
    }
}
