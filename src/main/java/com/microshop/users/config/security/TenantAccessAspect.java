package com.microshop.users.config.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Aspect que valida companyId solicitado vs JWT del usuario autenticado.
 * Defiende contra cross-tenant data leak.
 * Ver patrón completo en learnings.md (sesión 2026-04-27).
 */
@Aspect
@Component
@Slf4j
public class TenantAccessAspect {

    @Before("@annotation(com.microshop.users.config.security.RequiresTenantAccess)")
    public void validate(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresTenantAccess annotation = method.getAnnotation(RequiresTenantAccess.class);

        Long requestedCompanyId = extractRequestedCompanyId(joinPoint, annotation.paramName());
        if (requestedCompanyId == null) {
            log.warn("@RequiresTenantAccess en {}.{} pero no se pudo resolver companyId — saltando",
                    method.getDeclaringClass().getSimpleName(), method.getName());
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Sin autenticación — no se puede validar tenant");
        }

        if (annotation.allowSuperAdmin() && hasRole(auth, "ROLE_SUPERADMIN")) {
            log.debug("Bypass tenant check para SUPERADMIN en {}.{}",
                    method.getDeclaringClass().getSimpleName(), method.getName());
            return;
        }

        Long jwtCompanyId = extractJwtCompanyId(auth);
        if (jwtCompanyId == null) {
            throw new AccessDeniedException("JWT sin claim companyId — no se puede validar tenant");
        }

        if (!jwtCompanyId.equals(requestedCompanyId)) {
            log.warn("CROSS-TENANT BLOCKED: user companyId={} intentó acceder a companyId={} en {}.{}",
                    jwtCompanyId, requestedCompanyId,
                    method.getDeclaringClass().getSimpleName(), method.getName());
            throw new AccessDeniedException(
                    "Acceso denegado: el companyId solicitado (" + requestedCompanyId
                            + ") no coincide con el del usuario autenticado (" + jwtCompanyId + ")");
        }
    }

    private Long extractRequestedCompanyId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < paramNames.length; i++) {
            if (paramName.equals(paramNames[i]) && args[i] != null) {
                return toLong(args[i]);
            }
        }
        for (Object arg : args) {
            if (arg == null) continue;
            try {
                Method m = arg.getClass().getMethod("companyId");
                Object v = m.invoke(arg);
                if (v != null) return toLong(v);
            } catch (NoSuchMethodException ignored) {
                try {
                    Method m = arg.getClass().getMethod("getCompanyId");
                    Object v = m.invoke(arg);
                    if (v != null) return toLong(v);
                } catch (Exception ignored2) { }
            } catch (Exception ignored) { }
        }
        return null;
    }

    private Long extractJwtCompanyId(Authentication auth) {
        Object details = auth.getDetails();
        if (details instanceof Map<?, ?> map) {
            Object v = map.get("companyId");
            if (v != null) return toLong(v);
        }
        return null;
    }

    private boolean hasRole(Authentication auth, String role) {
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (role.equals(ga.getAuthority())) return true;
        }
        return false;
    }

    private Long toLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        try { return Long.parseLong(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
