package com.microshop.users.aislamiento;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Suspensión de tenant (B09 del inventario original, B08 en el estado del loop).
 *
 * <p>Hasta 2026-07-28 la suspensión de una empresa NO existía funcionalmente: el soft-delete de
 * {@code DELETE /users/api/companies/{id}} ponía {@code company.is_active = false}, pero
 * {@code AuthCommandService.validateCompanyMembership} solo comprobaba
 * {@code userCompany.isActive()} — el estado de la MEMBRESÍA, no el de la empresa — así que los
 * usuarios de un tenant "suspendido" seguían entrando y operando con total normalidad. Y por si
 * fuera poco, {@code InitialUserConfig} reactivaba la empresa demo en cada arranque, de modo que
 * suspenderla tampoco sobrevivía a un reinicio.</p>
 *
 * <p>Corre contra la BD de desarrollo (no hay Docker) y limpia sus datos en {@link #limpiar()}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Suspensión de tenant: una empresa desactivada no puede operar")
class SuspensionDeTenantTest {

    private static final String RUC = "20999000033";
    private static final String EMAIL = "admin.suspension@test.local";
    private static final String PASSWORD = "Suspension2026";

    private final MockMvc mockMvc;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    SuspensionDeTenantTest(MockMvc mockMvc, JdbcTemplate jdbc) {
        this.mockMvc = mockMvc;
        this.jdbc = jdbc;
    }

    private long companyId;

    @BeforeAll
    void registrarTenant() throws Exception {
        limpiar();
        MvcResult res = mockMvc.perform(post("/users/api/saas/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Empresa Suspendible",
                                  "ruc": "%s",
                                  "adminEmail": "%s",
                                  "adminPassword": "%s",
                                  "adminNombres": "Admin",
                                  "adminApellidos": "Suspension",
                                  "planCode": "STARTER"
                                }
                                """.formatted(RUC, EMAIL, PASSWORD)))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(201);
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        companyId = json.get("companyId").asLong();
    }

    private int intentarLogin() throws Exception {
        return mockMvc.perform(post("/users/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "%s", "companyId": %d}
                                """.formatted(EMAIL, PASSWORD, companyId)))
                .andReturn().getResponse().getStatus();
    }

    @Test
    @Order(1)
    @DisplayName("control: con la empresa activa, el admin entra")
    void controlSanoConEmpresaActiva() throws Exception {
        assertThat(intentarLogin())
                .as("una empresa activa debe permitir el login; si esto falla, el fixture está mal")
                .isEqualTo(200);
    }

    @Test
    @Order(2)
    @DisplayName("con la empresa suspendida, el login queda bloqueado")
    void empresaSuspendidaBloqueaElLogin() throws Exception {
        jdbc.update("update dbshopusuarios.company set is_active = false where id = ?", companyId);

        int status = intentarLogin();

        assertThat(status)
                .as("una empresa suspendida no puede seguir operando. Antes de este fix el login "
                        + "devolvía 200 porque solo se miraba el estado de la membresía.")
                .isNotEqualTo(200);
    }

    @Test
    @Order(3)
    @DisplayName("reactivar la empresa restaura el acceso")
    void reactivarRestauraElAcceso() throws Exception {
        jdbc.update("update dbshopusuarios.company set is_active = true where id = ?", companyId);

        assertThat(intentarLogin())
                .as("la suspensión debe ser reversible")
                .isEqualTo(200);
    }

    @AfterAll
    void limpiar() {
        Long usuarioId = unicoLong("select id from dbshopusuarios.usuario where username = ?", EMAIL);
        if (usuarioId != null) {
            Long personaId = unicoLong("select persona_id from dbshopusuarios.usuario where id = ?", usuarioId);
            jdbc.update("delete from dbshopusuarios.sesion where usuario_id = ?", usuarioId);
            jdbc.update("delete from dbshopusuarios.notifications where user_id = ?", usuarioId);
            jdbc.update("delete from dbshopusuarios.vendedor where usuario_id = ?", usuarioId);
            jdbc.update("delete from dbshopusuarios.user_company where usuario_id = ?", usuarioId);
            // Tablas de auditoria de Envers (M05): el @AfterAll no las conocia y la suite dejaba
            // filas de revision de sus propios fixtures. Se borran ANTES que la fila real, porque
            // las _aud tienen FK hacia revinfo pero no hacia la tabla auditada: el orden importa
            // solo para que no queden huerfanas.
            jdbc.update("delete from dbshopusuarios.usuario_aud where id = ?", usuarioId);
            jdbc.update("delete from dbshopusuarios.user_company_aud where usuario_id = ?", usuarioId);
            jdbc.update("delete from dbshopusuarios.usuario where id = ?", usuarioId);
            if (personaId != null) {
                jdbc.update("delete from dbshopusuarios.persona where id = ?", personaId);
            }
        }
        Long cid = unicoLong("select id from dbshopusuarios.company where ruc = ?", RUC);
        if (cid != null) {
            jdbc.update("delete from dbshopusuarios.user_company where company_id = ?", cid);
            jdbc.update("delete from dbshopusuarios.company_module where company_id = ?", cid);
            jdbc.update("delete from dbshopusuarios.company_rubro where company_id = ?", cid);
            jdbc.update("delete from dbshopusuarios.saas_subscription where company_id = ?", cid);
            jdbc.update("delete from dbshopusuarios.user_company_aud where company_id = ?", cid);
            jdbc.update("delete from dbshopusuarios.company_aud where id = ?", cid);
            jdbc.update("delete from dbshopusuarios.company where id = ?", cid);
        }
    }

    private Long unicoLong(String sql, Object arg) {
        List<Long> filas = jdbc.queryForList(sql, Long.class, arg);
        return filas.isEmpty() ? null : filas.get(0);
    }
}
