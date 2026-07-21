package com.microshop.users.shared.constants;

import java.math.RoundingMode;

/**
 * Constantes transversales del servicio (Fase 1 — "cimientos", ver
 * audit-hardening-2026-05-28). Agrupa literales hoy dispersos en
 * SecurityConfig, TenantAccessAspect, AuthCommandService, PayrollController,
 * ChatController, PayrollCommandService, ContractMapper y AnalyticsQueryService.
 *
 * <p>NO absorbe {@link ApiPaths}, que se mantiene como clase aparte.</p>
 *
 * <p>Fase 1 es puramente aditiva: estas constantes todavía NO reemplazan los
 * literales originales en sus archivos fuente (eso ocurre en una fase
 * posterior, F2, tras validar que no rompe nada).</p>
 */
public final class AppConstants {

    private AppConstants() {
    }

    /**
     * Roles y headers de seguridad. Ver SecurityConfig.java, TenantAccessAspect.java,
     * AuthCommandService.java, PayrollController.java y ChatController.java.
     */
    public static final class Seguridad {

        private Seguridad() {
        }

        // Roles SIN prefijo "ROLE_" — para usar con hasRole()/hasAnyRole() en
        // @PreAuthorize y en SecurityConfig (Spring Security antepone "ROLE_" internamente).
        public static final String ADMIN = "ADMIN";
        public static final String SUPERADMIN = "SUPERADMIN";
        public static final String GERENTE = "GERENTE";
        public static final String SOPORTE = "SOPORTE";

        // Authorities/roles CON prefijo "ROLE_" — para usar con hasAuthority() o
        // comparación directa contra GrantedAuthority.getAuthority().
        public static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";
        public static final String ROLE_ADMIN = "ROLE_ADMIN";
        public static final String ROLE_INTERNAL_SERVICE = "ROLE_INTERNAL_SERVICE";

        // Expresión SpEL compile-time usada en @PreAuthorize de PayrollController
        // para endpoints accesibles por ADMIN humano o por llamadas internas s2s.
        public static final String ADMIN_OR_INTERNAL =
                "hasRole('ADMIN') or hasAuthority('ROLE_INTERNAL_SERVICE')";

        // Headers HTTP — ver WebClientConfig.java (s2s) y TenantAccessAspect.java (tenant).
        public static final String X_INTERNAL_TOKEN = "X-Internal-Token";
        public static final String X_TENANT_ID = "X-Tenant-ID";
    }

    /** Defaults de paginación para endpoints con @RequestParam page/size. */
    public static final class Paginacion {

        private Paginacion() {
        }

        public static final String DEFAULT_PAGE = "0";
        public static final String DEFAULT_SIZE = "20";
        public static final int MAX_SIZE = 200;
    }

    /** Escala y redondeo estándar para montos monetarios (soles). */
    public static final class Money {

        private Money() {
        }

        public static final int ESCALA = 2;
        public static final int ESCALA_RATIO = 4;
        public static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
    }

    /**
     * Constantes de negocio de planilla peruana, hoy hardcodeadas en
     * PayrollCommandService.calcularPlanillaPeruana()/calcularRenta5ta(),
     * y de vencimiento de contrato en ContractMapper/AnalyticsQueryService.
     */
    public static final class Negocio {

        private Negocio() {
        }

        // PayrollCommandService — tarifa hora extra: sueldoBase / HORAS_MES_LEGAL.
        public static final java.math.BigDecimal HORAS_MES_LEGAL = new java.math.BigDecimal("240");

        // PayrollCommandService — recargo hora extra 25% (DL 854 art. 10, primeras 2h).
        public static final java.math.BigDecimal RECARGO_HORA_EXTRA = new java.math.BigDecimal("1.25");

        // PayrollCommandService — proyección anual de renta 5ta: sueldoBase * 14 (12 + 2 grat).
        public static final int MESES_PROYECCION_ANUAL = 14;

        // PayrollCommandService — deducción de 7 UIT anuales para renta 5ta (art. 53 LIR).
        public static final int UIT_DEDUCCION_RENTA5TA = 7;

        // PayrollCommandService — CTS: 1/6 de la gratificación semestral.
        public static final int DIVISOR_GRATIFICACION = 6;

        // PayrollCommandService — divisor mensual del impuesto anual de renta 5ta.
        public static final int MESES_ANIO = 12;

        // ContractMapper.java / AnalyticsQueryService.java — ventana de alerta de
        // vencimiento de contrato (fechaFin dentro de los próximos N días).
        public static final int DIAS_ALERTA_VENCIMIENTO_CONTRATO = 30;
    }
}
