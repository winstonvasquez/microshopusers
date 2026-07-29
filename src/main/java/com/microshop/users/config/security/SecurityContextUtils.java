package com.microshop.users.config.security;

import com.microshop.users.shared.constants.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * Extrae companyId/rol del JWT autenticado. Misma logica que {@link TenantAccessAspect}
 * pero para casos donde hace falta FILTRAR resultados (no solo bloquear/permitir un companyId puntual).
 */
public final class SecurityContextUtils {

    private SecurityContextUtils() {
    }

    /** companyId del JWT autenticado, o null si no hay autenticacion/claim. */
    public static Long currentCompanyId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object details = auth.getDetails();
        if (details instanceof Map<?, ?> map) {
            Object v = map.get("companyId");
            if (v instanceof Number n) return n.longValue();
            if (v != null) {
                try {
                    return Long.parseLong(v.toString());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    /**
     * userId del JWT autenticado, o null si no hay autenticación/claim. Necesario para distinguir
     * "consulto mis propios datos" de "consulto los de otro": el cambio de empresa del header
     * necesita que un usuario vea TODAS sus membresías, incluso las de otras empresas.
     */
    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object details = auth.getDetails();
        if (details instanceof Map<?, ?> map) {
            Object v = map.get("userId");
            if (v instanceof Number n) return n.longValue();
            if (v != null) {
                try {
                    return Long.parseLong(v.toString());
                } catch (NumberFormatException ignored) {
                    // claim presente pero no numérico: se trata como ausente
                }
            }
        }
        return null;
    }

    /** true si el usuario autenticado tiene ROLE_SUPERADMIN (bypass de scoping por tenant). */
    public static boolean isSuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (AppConstants.Seguridad.ROLE_SUPERADMIN.equals(ga.getAuthority())) return true;
        }
        return false;
    }
}
