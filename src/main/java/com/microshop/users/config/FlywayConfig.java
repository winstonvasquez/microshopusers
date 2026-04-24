package com.microshop.users.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

/**
 * Configuración multi-schema de Flyway.
 * Ejecuta migraciones en dos schemas independientes:
 * - dbshopusuarios: usuarios, empresas, autenticación
 * - dbshoprrhh: empleados, planillas, vacaciones, evaluaciones
 */
@Configuration
public class FlywayConfig {

    /**
     * Migración del schema de usuarios (ejecuta primero por @Order(1)).
     */
    @Bean
    @Order(1)
    public Flyway flywayUsuarios(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("dbshopusuarios")
                .defaultSchema("dbshopusuarios")
                .locations("classpath:db/migration/usuarios")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .createSchemas(true)
                .validateOnMigrate(false)
                .load();
        flyway.repair();
        flyway.migrate();
        return flyway;
    }

    /**
     * Migración del schema de RRHH (ejecuta segundo por @Order(2)).
     */
    @Bean
    @Order(2)
    public Flyway flywayRrhh(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("dbshoprrhh")
                .defaultSchema("dbshoprrhh")
                .locations("classpath:db/migration/rrhh")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .createSchemas(true)
                .validateOnMigrate(false)
                .load();
        flyway.repair();
        flyway.migrate();
        return flyway;
    }
}
