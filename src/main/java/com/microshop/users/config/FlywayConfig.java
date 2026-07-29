package com.microshop.users.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

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
// Los servicios arrancan con spring.main.lazy-initialization=true (ver erp.ps1):
// sin @Lazy(false) este bean no se instancia y las migraciones nunca se aplican.
@Lazy(false)
@RequiredArgsConstructor
@Slf4j
public class FlywayConfig {

    private final DataSource dataSource;

    /**
     * Version de baseline del schema `usuarios` (D01). Solo tiene efecto en un esquema SIN tabla de
     * historial, o sea en un entorno NUEVO; en el actual no cambia nada.
     *
     * <p>La propiedad es por schema —no {@code spring.flyway.baseline-version}— porque los dos schemas
     * de este servicio necesitan valores distintos: `usuarios` parte de un dump y `rrhh` se reconstruye
     * de cero. Ver la decision D01 del backlog.</p>
     */
    @Value("${spring.flyway.baseline-version-usuarios:0}")
    private String baselineUsuarios;

    /**
     * Validacion del historial contra las migraciones del repo (M36).
     *
     * <p>Estaba en false. Junto con el {@code repair()} incondicional formaba una pareja que
     * garantizaba que Flyway no avisara nunca de nada: la validacion desactivada no compara, y el
     * repair reescribe el historial para que coincida con el repo en lugar de detectar la deriva.
     * Ese silencio dejo pasar dos defectos reales en logistica: la tabla {@code processed_movements}
     * que ninguna migracion habia creado (M35) y siete tablas con {@code created_at NOT NULL} sin
     * mapear (M49). Se comprobo en runtime que con la validacion activada este entorno arranca.</p>
     *
     * <p>Se puede apagar por configuracion si un entorno concreto tiene el historial sucio y hay que
     * arrancar antes de limpiarlo.</p>
     */
    @Value("${spring.flyway.validate-on-migrate:true}")
    private boolean validateOnMigrate;

    /**
     * Si se ejecuta {@code repair()} antes de migrar (M36).
     *
     * <p>Corria siempre y sin condicion, y eso no es una red de seguridad sino un borrador de
     * evidencia: {@code repair} alinea descripcion y checksum de las filas del historial con lo que
     * hay en el repo, asi que una migracion aplicada y luego editada pasa desapercibida. Reparar es
     * una operacion deliberada, no algo que ocurra en cada arranque: se activa a mano con
     * {@code --spring.flyway.repair-on-migrate=true}.</p>
     */
    @Value("${spring.flyway.repair-on-migrate:false}")
    private boolean repairOnMigrate;

    @PostConstruct
    public void migrateAllSchemas() {
        // D01: el schema `usuarios` NO es autoconsistente —V5 en adelante asume `erp_parameters`
        // creada y ninguna migracion la crea—, asi que un entorno nuevo parte de un dump y se
        // declara el punto de partida. `rrhh` SI lo es desde que V9 crea sus tablas de Envers,
        // por eso se queda en 0 y se reconstruye de cero sin dump.
        migrateSchema("dbshopusuarios", "classpath:db/migration/usuarios", baselineUsuarios);
        migrateSchema("dbshoprrhh", "classpath:db/migration/rrhh", "0");
    }

    private void migrateSchema(String schema, String location, String baselineVersion) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .locations(location)
                .baselineOnMigrate(true)
                .baselineVersion(baselineVersion)
                .createSchemas(true)
                .validateOnMigrate(validateOnMigrate)
                .load();

        if (repairOnMigrate) {
            log.warn("Flyway[{}]: repair() activado por configuracion — se van a reescribir descripciones y checksums del historial.", schema);
            flyway.repair();
        }

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
