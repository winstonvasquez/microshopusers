package com.microshop.users.config.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un endpoint como protegido por validación de tenant.
 * Ver javadoc completo en {@link TenantAccessAspect}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresTenantAccess {
    String paramName() default "companyId";
    boolean allowSuperAdmin() default false;
}
