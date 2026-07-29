package com.microshop.users.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.microshop.users.shared.constants.AppConstants;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Autenticación servicio-a-servicio vía cabecera {@code X-Internal-Token}, igual que en
 * ventas/compras/contabilidad/logistica. microshopusers era el ÚNICO servicio sin ella.
 *
 * <p><b>Por qué se añadió (iteración 4, 2026-07-29):</b> el KPI de RRHH del dashboard ejecutivo
 * (repoanalitica → {@code GET /hr/api/employees/paged}) devolvía cero para TODOS los tenants. No
 * era un fallo de cálculo: repoanalitica llama sin JWT de usuario, con solo el token interno, y
 * como aquí no existía ningún filtro que lo reconociera, la petición moría en
 * {@code anyRequest().authenticated()}. El {@code catch} de {@code logKpiFallback} lo degradaba a
 * cero en silencio, así que la tarjeta "empleados activos" del dashboard llevaba mostrando 0 sin
 * que nada lo señalara.</p>
 *
 * <p><b>El tenant viaja en la cabecera, y eso es imprescindible, no un extra.</b> Los query
 * services de RRHH resuelven el alcance con {@code TenantContext.getCurrentTenantId()}, que lee
 * {@code companyId} de los <em>details</em> de la autenticación y <b>lanza</b> si no lo encuentra.
 * Un filtro s2s que solo concediera el rol dejaría el contexto sin {@code companyId}: la llamada
 * pasaría la cadena de seguridad y reventaría después con 500, o —bastante peor si algún día ese
 * contexto dejara de ser fail-closed— devolvería los empleados de TODA la plataforma dentro del
 * dashboard de una sola empresa. Por eso se puebla {@code companyId} desde {@code X-Tenant-ID}: la
 * llamada interna queda acotada al tenant que la originó, exactamente igual que si viniera con el
 * JWT de un usuario de esa empresa.</p>
 *
 * <p>Si la cabecera {@code X-Tenant-ID} no viene o no es numérica, se concede el rol pero NO se
 * puebla el tenant: {@code TenantContext} seguirá lanzando y el acceso a datos por empresa queda
 * denegado. Es deliberado — es preferible que una integración mal configurada falle a que lea de
 * más.</p>
 */
@Component
@Slf4j
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = AppConstants.Seguridad.X_INTERNAL_TOKEN;
    public static final String ROLE = AppConstants.Seguridad.ROLE_INTERNAL_SERVICE;
    public static final String PRINCIPAL = "internal-service";

    private final String expectedToken;

    public InternalServiceAuthenticationFilter(
            @Value("${microshop.internal.token:}") String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (StringUtils.hasText(expectedToken)) {
            String received = request.getHeader(HEADER);
            if (received != null && received.equals(expectedToken)) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                PRINCIPAL,
                                null,
                                List.of(new SimpleGrantedAuthority(ROLE)));

                Long tenantId = leerTenantDeCabecera(request);
                if (tenantId != null) {
                    // Misma forma que produce JwtAuthenticationFilter, para que TenantContext y el
                    // resto de consumidores no tengan que distinguir el origen de la petición.
                    auth.setDetails(Map.of("companyId", tenantId));
                }

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Auth s2s aceptada en {} (tenant={})", request.getRequestURI(), tenantId);
            }
        }

        chain.doFilter(request, response);
    }

    private Long leerTenantDeCabecera(HttpServletRequest request) {
        String raw = request.getHeader(AppConstants.Seguridad.X_TENANT_ID);
        if (!StringUtils.hasText(raw)) return null;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            log.warn("Cabecera {} no numérica en llamada s2s a {}: {}",
                    AppConstants.Seguridad.X_TENANT_ID, request.getRequestURI(), raw);
            return null;
        }
    }
}
