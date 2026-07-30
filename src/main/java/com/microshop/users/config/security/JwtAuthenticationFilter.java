package com.microshop.users.config.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    /**
     * @Lazy rompe el ciclo de dependencias: SesionRevocacionService -> SesionRepository -> EntityManager,
     * y este filtro se construye dentro de la cadena de seguridad, que Spring inicializa antes.
     */
    private final com.microshop.users.application.command.SesionRevocacionService sesionRevocacionService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService,
            @org.springframework.context.annotation.Lazy
            com.microshop.users.application.command.SesionRevocacionService sesionRevocacionService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.sesionRevocacionService = sesionRevocacionService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // M27: la firma y la expiración no bastan. Un token de una empresa suspendida (o de una
                // sesión cerrada) sigue siendo criptográficamente válido hasta que caduca, así que
                // suspender un tenant no cortaba a quien ya estuviera dentro — hasta 24 h operando con
                // normalidad. Aquí se consulta la sesión, que es barata porque este servicio es dueño
                // de la tabla. Un jti desconocido se deja pasar a propósito: las sesiones anteriores a
                // V40 no lo tienen guardado, y la firma ya se validó, así que «desconocido» significa
                // «token legítimo antiguo», no falsificación. Ver SesionRevocacionService.
                if (jwtService.isTokenValid(jwt, userDetails)
                        && !sesionRevocacionService.estaRevocado(jwtService.extractJti(jwt))) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    // Extraer companyId y userId del JWT para que estén disponibles en authentication.details
                    Map<String, Object> details = new HashMap<>();
                    try {
                        Object companyId = jwtService.extractClaim(jwt, c -> c.get("companyId"));
                        Object userId = jwtService.extractClaim(jwt, c -> c.get("userId"));
                        if (companyId != null) details.put("companyId", companyId);
                        if (userId != null) details.put("userId", userId);
                    } catch (Exception ignored) { }
                    authToken.setDetails(details);
                    // M06: publica los modulos del plan en el request para que ModuloContratadoFilter
                    // los lea sin volver a parsear el token. Se usa un atributo y no `Authentication`
                    // porque los seis servicios pueblan Authentication de tres formas distintas y
                    // ninguna incluia los modulos: el atributo es un canal uniforme que no obliga a
                    // normalizar las tres a la vez (eso es D3). El nombre del claim es el del
                    // contrato de microshopusers.config.security.JwtClaims.
                    try {
                        Object modulosCrudo = jwtService.extractClaim(jwt, c -> c.get("modules"));
                        if (modulosCrudo != null) {
                            request.setAttribute("microshop.modulosContratados",
                                    java.util.Arrays.stream(modulosCrudo.toString().split(","))
                                            .map(String::trim)
                                            .filter(s -> !s.isEmpty())
                                            .collect(java.util.stream.Collectors.toUnmodifiableSet()));
                        }
                    } catch (Exception ignoradoModulos) {
                        // Un token sin el claim no es un error: ver la nota de ModuloContratadoFilter.
                    }

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException ex) {
            log.debug("Token JWT inválido rechazado: {}", ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
