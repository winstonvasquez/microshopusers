package com.microshop.users.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exige que el tenant tenga contratado el modulo al que pertenece el endpoint (M06).
 *
 * <h2>Que estaba roto</h2>
 * <p>El plan contratado viajaba en el claim {@code modules} del JWT y <b>ningun backend lo leia</b>:
 * {@code grep getClaim("modules")} en los cuatro servicios de negocio daba 0 resultados. Se usaba solo
 * para pintar u ocultar el menu en el frontend ({@code hasModule()}), que es una comodidad de UI y no un
 * control de acceso: cualquiera con un token de un plan STARTER podia llamar directamente a los
 * endpoints de compras, contabilidad o RRHH sin haberlos pagado.</p>
 *
 * <p>Y habia una segunda mitad, peor: {@code SaasQueryService.getEnabledModuleCodes} devolvia
 * <b>todos</b> los modulos cuando la empresa no tenia suscripcion, asi que 4 de las 7 empresas de la
 * base recibian el catalogo completo en el claim. Corregido en users junto con la creacion de la
 * suscripcion en el alta de empresa; sin eso, imponer esto aqui habria sido teatro.</p>
 *
 * <h2>Reparto de este servicio</h2>
 * <pre>/hr/api/** -> RRHH\n *  Los endpoints de /users/api NO llevan modulo: son plataforma\n *  (auth, empresas, planes), no una funcionalidad que se contrate.</pre>
 *
 * <p>Se usa un filtro con mapa de prefijos y no una anotacion por endpoint porque el reparto
 * endpoint-modulo en este ERP es el clasico y sigue exactamente los prefijos de ruta que ya existen.
 * Anotar cada controlador daria el mismo resultado con decenas de sitios donde olvidarse de uno, y un
 * endpoint sin anotar queda abierto — que es el modo de fallo que se esta corrigiendo.</p>
 *
 * <h2>A quien NO se aplica</h2>
 * <ul>
 *   <li><b>Peticiones sin autenticar:</b> no hay tenant al que exigirle plan.</li>
 *   <li><b>Llamadas s2s</b> ({@code ROLE_INTERNAL_SERVICE}): el Outbox de otro servicio no tiene plan.
 *       Bloquearlas rompería las cadenas venta-asiento-stock por un motivo que no aplica.</li>
 *   <li><b>{@code /actuator/**}</b> y las rutas que no caen en ningun modulo del mapa.</li>
 * </ul>
 *
 * <p><b>Se deja pasar el token SIN el claim</b> {@code modules}: los emitidos antes de este cambio no
 * lo llevan y rechazarlos expulsaria a todos los usuarios con sesion abierta al desplegar. La ventana
 * se cierra sola en 24 h (vida del token) y queda en el log. Un token CON el claim y sin el modulo si
 * se rechaza, que es el caso que importa.</p>
 */
@Component
@Slf4j
public class ModuloContratadoFilter extends OncePerRequestFilter {

    /**
     * Prefijo de ruta -> modulo exigido. El orden importa: se elige la coincidencia MAS LARGA, para que
     * un prefijo especifico gane al general sin depender del orden de declaracion.
     */
    private static final Map<String, String> MODULO_POR_PREFIJO = Map.of(
            "/hr/api", "RRHH"
    );

    private static final String ROL_INTERNO = "ROLE_INTERNAL_SERVICE";
    private static final String ATRIBUTO_MODULOS = "microshop.modulosContratados";

    /**
     * ObjectMapper propio y no inyectado: no todos los servicios exponen un bean de ObjectMapper
     * —users y contabilidad no lo tienen— y este filtro solo serializa un Map de cinco claves, asi
     * que depender de la configuracion de Jackson del servicio no aporta nada y hacia que el
     * contexto no arrancara. Lo destapo el arranque, no la compilacion.
     */
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String moduloExigido = moduloDe(request.getRequestURI());
        if (moduloExigido == null || !aplica()) {
            filterChain.doFilter(request, response);
            return;
        }

        Set<String> modulos = modulosDelRequest(request);
        if (modulos == null) {
            log.debug("Peticion a {} con token sin claim `modules`: se permite (token anterior a M06)",
                    request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        if (!modulos.contains(moduloExigido)) {
            log.warn("Acceso denegado a {}: el plan del tenant no incluye el modulo {} (tiene: {})",
                    request.getRequestURI(), moduloExigido, modulos);
            responderModuloNoContratado(response, request.getRequestURI(), moduloExigido);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** Modulo exigido por la ruta, o {@code null} si no cae en ninguno del mapa. */
    private static String moduloDe(String ruta) {
        String mejor = null;
        int largo = -1;
        for (Map.Entry<String, String> e : MODULO_POR_PREFIJO.entrySet()) {
            if (ruta.contains(e.getKey()) && e.getKey().length() > largo) {
                mejor = e.getValue();
                largo = e.getKey().length();
            }
        }
        return mejor;
    }

    /** {@code true} si a esta peticion hay que exigirle plan. */
    private static boolean aplica() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .noneMatch(ROL_INTERNO::equals);
    }

    /**
     * Modulos del token, o {@code null} si no vienen.
     *
     * <p>Los toma del atributo de request que deja {@code JwtAuthenticationFilter} al validar el token,
     * en vez de volver a parsearlo. Es tambien un paso hacia D3: hoy los seis servicios pueblan
     * {@code Authentication} de tres formas distintas y ninguna incluia los modulos; el atributo es un
     * canal uniforme que no obliga a cambiar las tres a la vez.</p>
     */
    @SuppressWarnings("unchecked")
    private static Set<String> modulosDelRequest(HttpServletRequest request) {
        Object crudo = request.getAttribute(ATRIBUTO_MODULOS);
        if (crudo == null) return null;
        if (crudo instanceof Set<?> s) return (Set<String>) s;
        return Arrays.stream(crudo.toString().split(","))
                .map(String::trim).filter(x -> !x.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /** 403 con problem-detail, mismo formato que el resto de errores del servicio. */
    private void responderModuloNoContratado(HttpServletResponse response, String ruta, String modulo)
            throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("type", "urn:users:modulo-no-contratado");
        cuerpo.put("title", "Modulo no contratado");
        cuerpo.put("status", HttpStatus.FORBIDDEN.value());
        cuerpo.put("detail", "El plan contratado no incluye el modulo " + modulo
                + ". Actualice el plan para usar esta funcionalidad.");
        cuerpo.put("instance", ruta);
        cuerpo.put("modulo", modulo);
        JSON.writeValue(response.getOutputStream(), cuerpo);
    }
}
