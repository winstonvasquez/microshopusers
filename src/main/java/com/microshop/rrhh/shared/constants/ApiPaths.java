package com.microshop.rrhh.shared.constants;

/**
 * Paths de la API centralizados para evitar strings duplicados entre
 * controllers y documentación Swagger.
 */
public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String BASE = "/hr/api";
    public static final String EMPLOYEES = BASE + "/employees";
    public static final String ATTENDANCE = BASE + "/attendance";
    public static final String VACATIONS = BASE + "/vacations";
    public static final String PAYROLL = BASE + "/payroll";
    public static final String CONFIGURACION = BASE + "/configuracion";
    public static final String DEPARTMENTS = BASE + "/departments";
    public static final String POSITIONS = BASE + "/positions";
    public static final String CONTRACTS = BASE + "/contracts";
    public static final String EVALUATIONS = BASE + "/evaluations";
    public static final String GOALS = BASE + "/goals";
    public static final String TRAININGS = BASE + "/trainings";
    public static final String SELF_SERVICE = BASE + "/self-service";
}
