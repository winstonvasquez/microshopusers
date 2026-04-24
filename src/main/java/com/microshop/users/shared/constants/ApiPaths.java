package com.microshop.users.shared.constants;

/**
 * Paths de la API centralizados para evitar strings duplicados entre
 * controllers y documentación Swagger.
 */
public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String BASE = "/users/api";
    public static final String AUTH = BASE + "/auth";
    public static final String USERS = BASE + "/users";
    public static final String COMPANIES = BASE + "/companies";
    public static final String ROLES = BASE + "/roles";
    public static final String USER_COMPANIES = BASE + "/user-companies";
    public static final String VENDEDORES = BASE + "/v1/vendedores";
    public static final String SYSTEM_PARAMETERS = BASE + "/system/parameters";
    public static final String SAAS   = BASE + "/saas";
    public static final String TENANT = BASE + "/tenant";
    public static final String THEMES              = BASE + "/themes";
    public static final String CLIENTE_DIRECCIONES = BASE + "/clientes/me/direcciones";
    public static final String SEGMENTS            = BASE + "/segments";
}
