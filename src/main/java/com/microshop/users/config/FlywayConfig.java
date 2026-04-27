package com.microshop.users.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuración multi-schema de Flyway.
 * Spring Boot 4.0.5 removió FlywayAutoConfiguration, así que ejecutamos
 * Flyway.migrate() manualmente en @PostConstruct (los @Bean previos nunca se
 * instanciaban porque nadie inyectaba el tipo Flyway).
 *
 * Schemas:
 * - dbshopusuarios: usuarios, empresas, autenticación
 * - dbshoprrhh: empleados, planillas, vacaciones, evaluaciones
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class FlywayConfig {

    private final DataSource dataSource;

    @PostConstruct
    public void migrateAllSchemas() {
        migrateSchema("dbshopusuarios", "classpath:db/migration/usuarios");
        migrateSchema("dbshoprrhh", "classpath:db/migration/rrhh");
    }

    private void migrateSchema(String schema, String location) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .locations(location)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .createSchemas(true)
                .validateOnMigrate(false)
                .load();

        flyway.repair();
        MigrateResult result = flyway.migrate();

        if (result.migrationsExecuted > 0) {
            log.info("Flyway[{}]: aplicó {} migraciones → versión {}",
                    schema, result.migrationsExecuted, result.targetSchemaVersion);
            for (var m : result.migrations) {
                log.info("  ✓ V{} - {}", m.version, m.description);
            }
        } else {
            var current = flyway.info().current();
            log.info("Flyway[{}]: schema al día (versión {})",
                    schema, current != null ? current.getVersion() : "<vacío>");
        }
    }
}
