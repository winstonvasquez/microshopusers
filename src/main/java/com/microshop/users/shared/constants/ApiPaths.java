package com.microshop.users.shared.constants;

/**
 * Paths de la API centralizados para evitar strings duplicados entre
 * controllers y documentación Swagger.
 */
public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String BASE = "/api";
    public static final String AUTH = BASE + "/auth";
    public static final String USERS = BASE + "/users";
    public static final String COMPANIES = BASE + "/companies";
    public static final String ROLES = BASE + "/roles";
    public static final String VENDORS = BASE + "/vendors";
    public static final String USER_COMPANIES = BASE + "/user-companies";
}
