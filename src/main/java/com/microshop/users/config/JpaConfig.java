package com.microshop.users.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuración JPA multi-módulo.
 * Escanea entidades y repositorios en ambos packages: users y rrhh.
 */
@Configuration
@EntityScan(basePackages = {
        "com.microshop.users",
        "com.microshop.rrhh"
})
@EnableJpaRepositories(basePackages = {
        "com.microshop.users",
        "com.microshop.rrhh"
})
public class JpaConfig {
}
