package com.microshop.users.config.security;

import com.microshop.users.shared.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        /**
         * Jerarquía de roles: sin ella, cada {@code hasRole}/{@code hasAnyRole} es una
         * comparación literal y un SUPERADMIN queda FUERA de todo lo restringido a ADMIN.
         *
         * <p>Síntoma concreto: la Bandeja de Soporte ({@code GET /users/api/admin/chat/conversaciones},
         * anotado {@code ADMIN_OR_SOPORTE}) respondía 403 al superadministrador. El mismo fallo
         * estaba en microshopcontabilidad y se resolvió igual.</p>
         *
         * <p>SUPERADMIN implica ADMIN, y ADMIN implica los roles funcionales. SOPORTE queda
         * aparte a propósito: es staff de atención, no un escalón de la cadena de mando.</p>
         */
        @Bean
        public RoleHierarchy roleHierarchy() {
                return RoleHierarchyImpl.withDefaultRolePrefix()
                                .role(AppConstants.Seguridad.SUPERADMIN).implies(AppConstants.Seguridad.ADMIN)
                                .role(AppConstants.Seguridad.ADMIN).implies(
                                                AppConstants.Seguridad.GERENTE,
                                                AppConstants.Seguridad.SOPORTE)
                                .build();
        }

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final InternalServiceAuthenticationFilter internalServiceAuthFilter;
        private final UserDetailsService userDetailsService;

        @Value("${app.cors.allowed-origins:http://localhost:4200}")
        private String allowedOriginsRaw;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeHttpRequests(auth -> auth
                                                // Hardening 2026-05-28 (CRIT-3): las MUTACIONES de parámetros ERP
                                                // (tasas SUNAT: UIT, ONP, ESSALUD) y de empresas requieren ADMIN.
                                                // El GET sigue público: lo consume la planilla s2s y el onboarding.
                                                // Los matchers restrictivos van ANTES del permitAll (primer match gana).
                                                // Binarios de imagen servidos desde BD (logo de empresa, foto de
                                                // empleado): GET publico y cacheado con ETag — se pintan en login,
                                                // tienda y fichas antes de tener sesion. Solo exponen los bytes,
                                                // ningun dato de negocio. Van PRIMERO para no heredar la regla
                                                // ADMIN/SUPERADMIN de /users/api/companies/** (primer match gana).
                                                // Las subidas (POST) siguen protegidas por las reglas de abajo.
                                                .requestMatchers(HttpMethod.GET, "/users/api/companies/*/logo").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/hr/api/employees/*/foto").permitAll()
                                                // B09 (2026-07-28): estos parámetros son GLOBALES (tenant_id IS NULL) y la
                                                // escritura tocaba esa fila única, así que con rol ADMIN cualquier empresa
                                                // podía cambiar IGV_RATE —la tasa nacional de IGV— para TODOS los tenants.
                                                // Son configuración de plataforma, no de empresa: solo SUPERADMIN escribe.
                                                // El GET sigue abierto a cualquier autenticado (la app necesita leer el IGV).
                                                .requestMatchers(HttpMethod.PUT, "/users/api/system/parameters/**").hasRole(AppConstants.Seguridad.SUPERADMIN)
                                                .requestMatchers(HttpMethod.POST, "/users/api/system/parameters/**").hasRole(AppConstants.Seguridad.SUPERADMIN)
                                                .requestMatchers(HttpMethod.DELETE, "/users/api/system/parameters/**").hasRole(AppConstants.Seguridad.SUPERADMIN)
                                                // GET incluido: /companies (list/paged/export/{id}) NO tenia
                                                // ninguna proteccion (caia al permitAll de abajo) — cualquiera,
                                                // sin login, podia listar TODAS las empresas. Unico consumidor
                                                // real es el panel admin (company.service.ts), nunca publico.
                                                .requestMatchers("/users/api/companies/**").hasAnyRole(AppConstants.Seguridad.ADMIN, AppConstants.Seguridad.SUPERADMIN)
                                                // Autoservicio del usuario autenticado (password propio, empresas propias,
                                                // switch de empresa activa, permisos propios) — cualquier rol, antes de
                                                // las reglas restrictivas de abajo para que /me/** nunca las herede.
                                                .requestMatchers("/users/api/users/me/**").authenticated()
                                                // Cambiar el ROL de un usuario (incl. otorgar SUPERADMIN) es más sensible
                                                // que el resto del CRUD de usuarios — restringido solo a SUPERADMIN.
                                                .requestMatchers(HttpMethod.PUT, "/users/api/users/*/role").hasRole(AppConstants.Seguridad.SUPERADMIN)
                                                // CRUD de usuarios (crear/editar/eliminar) — antes no tenía ninguna
                                                // restricción de rol, cualquier usuario autenticado podía crear/editar
                                                // a otros usuarios (incluyendo su propio rolId). Único consumidor real
                                                // es el panel admin (features/admin/services/user.service.ts).
                                                .requestMatchers(HttpMethod.POST, "/users/api/users").hasAnyRole(AppConstants.Seguridad.ADMIN, AppConstants.Seguridad.SUPERADMIN)
                                                .requestMatchers(HttpMethod.PUT, "/users/api/users/*").hasAnyRole(AppConstants.Seguridad.ADMIN, AppConstants.Seguridad.SUPERADMIN)
                                                .requestMatchers(HttpMethod.DELETE, "/users/api/users/*").hasAnyRole(AppConstants.Seguridad.ADMIN, AppConstants.Seguridad.SUPERADMIN)
                                                // Alta y aprobación de vendedores son acciones administrativas: antes
                                                // no tenían matcher y caían en anyRequest().authenticated(), así que
                                                // cualquier rol podía crear un perfil de vendedor o aprobarlo. El
                                                // aislamiento por empresa lo aporta además el scope de V37 (B07).
                                                .requestMatchers(HttpMethod.POST, "/users/api/v1/vendedores/**").hasAnyRole(AppConstants.Seguridad.ADMIN, AppConstants.Seguridad.SUPERADMIN)
                                                .requestMatchers(HttpMethod.PATCH, "/users/api/v1/vendedores/**").hasAnyRole(AppConstants.Seguridad.ADMIN, AppConstants.Seguridad.SUPERADMIN)
                                                // Configuración de planes SaaS (precios, módulos incluidos) es config
                                                // GLOBAL de la plataforma, no de un tenant — solo SUPERADMIN. Antes del
                                                // permitAll de /users/api/saas/** (primer match gana).
                                                .requestMatchers("/users/api/saas/admin/**").hasRole(AppConstants.Seguridad.SUPERADMIN)
                                                .requestMatchers(
                                                                "/users/api/auth/**",
                                                                "/users/api/saas/**",
                                                                "/users/api/system/parameters/**",
                                                                "/users/api/internal/**",
                                                                "/actuator/**",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**")
                                                .permitAll()
                                                // GET /users/api/themes/active es público
                                                .requestMatchers(HttpMethod.GET, "/users/api/themes/active")
                                                .permitAll()
                                                // Tema de empresa: solo ADMIN puede modificar
                                                .requestMatchers(HttpMethod.PUT, "/users/api/themes/company")
                                                .hasRole(AppConstants.Seguridad.ADMIN)
                                                .requestMatchers("/users/api/chat/**").authenticated()
                                                .requestMatchers("/users/api/admin/chat/**").hasAnyRole(AppConstants.Seguridad.ADMIN, AppConstants.Seguridad.SOPORTE)
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider())
                                // El filtro s2s va ANTES del de JWT: una llamada interna no trae
                                // Authorization, así que si corriera después no habría nada que
                                // autenticar y la petición moriría en anyRequest().authenticated().
                                // Es lo que dejaba el KPI de RRHH del dashboard ejecutivo en cero
                                // para todos los tenants.
                                .addFilterBefore(internalServiceAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                        CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of(allowedOriginsRaw.split(",")));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setExposedHeaders(List.of("Authorization"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
