package com.microshop.users.config.security;

import com.microshop.users.shared.constants.AppConstants;
import lombok.RequiredArgsConstructor;
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

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final UserDetailsService userDetailsService;

        @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:http://localhost:4200}")
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
                                                .requestMatchers(HttpMethod.PUT, "/users/api/system/parameters/**").hasRole(AppConstants.Seguridad.ADMIN)
                                                .requestMatchers(HttpMethod.POST, "/users/api/system/parameters/**").hasRole(AppConstants.Seguridad.ADMIN)
                                                .requestMatchers(HttpMethod.DELETE, "/users/api/system/parameters/**").hasRole(AppConstants.Seguridad.ADMIN)
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
