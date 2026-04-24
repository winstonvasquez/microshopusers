package com.microshop.rrhh.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TenantContext {

    public Long getCurrentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        // Caso 1: principal es Jwt (Spring OAuth2 Resource Server)
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            Object companyId = jwt.getClaim("companyId");
            return toLong(companyId, "companyId not found in JWT token");
        }

        // Caso 2: details contiene companyId como Map (JwtAuthenticationFilter custom)
        if (authentication.getDetails() instanceof Map<?, ?> details) {
            Object companyId = details.get("companyId");
            if (companyId != null) {
                return toLong(companyId, "Invalid companyId in details");
            }
        }

        throw new IllegalStateException("companyId not found in authentication context");
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return tryToLong(jwt.getClaim("userId"));
        }

        if (authentication.getDetails() instanceof Map<?, ?> details) {
            return tryToLong(details.get("userId"));
        }

        return null;
    }

    private Long toLong(Object value, String errMsg) {
        if (value == null) {
            throw new IllegalStateException(errMsg);
        }
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(errMsg, e);
        }
    }

    private Long tryToLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
