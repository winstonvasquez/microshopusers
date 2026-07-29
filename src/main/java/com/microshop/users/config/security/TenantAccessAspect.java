package com.microshop.users.config.security;

import com.microshop.users.shared.constants.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

        // La autenticación y los bypass se evalúan ANTES de resolver el tenant. Con fail-closed el
        // orden importa: al revés, una petición sin autenticar moriría con "no se pudo resolver
        // companyId" en vez de con su motivo real, y además el `return` de la rama no-resuelta
        // saltaba también esta comprobación de autenticación.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Sin autenticación — no se puede validar tenant");
        }

        // Bypass para llamadas s2s autenticadas vía X-Internal-Token. users tenía el
        // InternalServiceAuthenticationFilter pero NO este bypass, que es exactamente el bug que
        // fue M23 en ventas: su filtro concede ROLE_INTERNAL_SERVICE sin JWT, así que cualquier
        // endpoint anotado moriría con "no se pudo resolver companyId" para una llamada interna
        // legítima. Hoy no hay ningún llamador s2s que toque un endpoint anotado de este servicio,
        // o sea que es una mina latente y no una fuga activa — se cierra ahora porque en ventas
        // pasó de latente a real sin que nadie lo notara, degradado en silencio por un catch.
        //
        // Igual que en ventas y logística, además del rol se exige que el principal sea EXACTAMENTE
        // el literal del filtro: así, si algún día existiera un rol de BD llamado
        // "internal_service" asignado a un usuario real, su JWT no heredaría la confianza s2s.
        if (hasRole(auth, AppConstants.Seguridad.ROLE_INTERNAL_SERVICE)
                && InternalServiceAuthenticationFilter.PRINCIPAL.equals(String.valueOf(auth.getPrincipal()))) {
            log.debug("Bypass tenant check para llamada s2s en {}.{}",
                    method.getDeclaringClass().getSimpleName(), method.getName());
            return;
        }

        if (annotation.allowSuperAdmin() && hasRole(auth, AppConstants.Seguridad.ROLE_SUPERADMIN)) {
            log.debug("Bypass tenant check para SUPERADMIN en {}.{}",
                    method.getDeclaringClass().getSimpleName(), method.getName());
            return;
        }

        Long requestedCompanyId = extractRequestedCompanyId(joinPoint, annotation.paramName());
        if (requestedCompanyId == null) {
            // Fail-CLOSED: antes se hacía `log.warn` + `return` y la petición quedaba SIN validar.
            // No poder resolver el companyId es un bug de configuración del endpoint o un intento
            // de bypass (omitir el parámetro a propósito); en ambos casos se rechaza, no se permite.
            log.error("@RequiresTenantAccess en {}.{} no pudo resolver companyId solicitado — acceso denegado (fail-closed)",
                    method.getDeclaringClass().getSimpleName(), method.getName());
            throw new AccessDeniedException("No se pudo determinar el companyId solicitado — acceso denegado");
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

        // Estrategia 1b: el tenant viaja DENTRO del @RequestBody con el nombre real del paramName
        // (p.ej. `tenantId`), no como parámetro suelto. Sin esto, el fail-closed rechazaría
        // endpoints legítimos cuyo tenant SÍ viene, solo que en el cuerpo. Es el mismo fix que ya
        // se aplicó en contabilidad tras el incidente de tesorería (se validaba la cabecera
        // mientras el servicio escribía con el cuerpo).
        for (Object arg : args) {
            if (arg == null) continue;
            Long delCuerpo = leerAccesor(arg, paramName);
            if (delCuerpo != null) return delCuerpo;
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

        // Estrategia 3: header X-Tenant-ID en la request HTTP actual
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            HttpServletRequest req = sra.getRequest();
            String header = req.getHeader(AppConstants.Seguridad.X_TENANT_ID);
            if (header != null && !header.isBlank()) {
                Long parsed = toLong(header.trim());
                if (parsed != null) return parsed;
            }
        }
        return null;
    }

    /**
     * Lee {@code arg.<nombre>()} (estilo record) o {@code arg.get<Nombre>()} (estilo bean).
     * Devuelve {@code null} si el accesor no existe o el valor es nulo, para que la búsqueda
     * continúe con el resto de estrategias.
     */
    /**
     * Nombres aceptados como designadores del tenant en la estrategia 1b. Sin esta lista blanca,
     * un {@code @RequiresTenantAccess(paramName="id")} sobre un método SIN parámetro {@code id}
     * haría que 1b leyera {@code getId()} del cuerpo y comparara una PK de entidad contra el
     * companyId del JWT: 403 arbitrario, o peor, un permit si la PK coincidiera con el companyId.
     */
    private static boolean esNombreDeTenant(String nombre) {
        return "companyId".equals(nombre) || "tenantId".equals(nombre) || "empresaId".equals(nombre);
    }

    private Long leerAccesor(Object arg, String nombre) {
        if (!esNombreDeTenant(nombre)) return null;
        String getter = "get" + Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
        for (String candidato : new String[] { nombre, getter }) {
            try {
                Method m = arg.getClass().getMethod(candidato);
                Object v = m.invoke(arg);
                if (v != null) return toLong(v);
            } catch (NoSuchMethodException ignored) {
                // el argumento no expone ese campo: se prueba el siguiente candidato
            } catch (Exception e) {
                log.debug("No se pudo leer {}.{}(): {}", arg.getClass().getSimpleName(), candidato, e.toString());
            }
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
