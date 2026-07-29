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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Test de aislamiento multi-tenant de extremo a extremo — el control que el gate G5 del loop de
 * cierre de módulos exige y que hasta 2026-07-28 NO existía en ningún servicio del backend.
 *
 * <p>Es también el primer test de integración del repo: hasta ahora los 16 archivos de test eran
 * unitarios con Mockito y ninguno levantaba el contexto de Spring, así que ninguna de las brechas
 * de aislamiento halladas en la auditoría podía ser detectada por la suite.</p>
 *
 * <p><b>Qué prueba:</b> se dan de alta DOS empresas reales por la vía pública del producto
 * ({@code POST /users/api/saas/register}), cada una con su usuario ADMIN y su JWT auténtico
 * firmado por el propio servicio. Después se comprueba que el ADMIN del tenant A no puede leer,
 * modificar ni borrar nada del tenant B. Las aserciones de escritura no miran solo el código HTTP:
 * verifican <b>contra la base de datos</b> que el dato de B no cambió, que es la propiedad que de
 * verdad importa y la única que no se puede satisfacer por accidente.</p>
 *
 * <p><b>Base de datos:</b> corre contra la misma BD de desarrollo que el servicio (no hay Docker
 * en esta máquina, así que Testcontainers no es una opción). Los datos se crean con un RUC
 * reservado {@code 2099900xxxx} y se borran en {@link #limpiarDatosDePrueba()} respetando el orden
 * de claves foráneas.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Aislamiento multi-tenant: el ADMIN del tenant A no alcanza datos del tenant B")
class AislamientoMultiTenantTest {

    private static final String RUC_A = "20999000011";
    private static final String RUC_B = "20999000022";
    private static final String EMAIL_A = "admin.aislamiento.a@test.local";
    private static final String EMAIL_B = "admin.aislamiento.b@test.local";

    private final MockMvc mockMvc;
    private final JdbcTemplate jdbc;
    /** Local: en Boot 4 este contexto no expone un bean ObjectMapper y aquí solo se lee JSON. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    AislamientoMultiTenantTest(MockMvc mockMvc, JdbcTemplate jdbc) {
        this.mockMvc = mockMvc;
        this.jdbc = jdbc;
    }

    private Tenant a;
    private Tenant b;
    private long vendedorIdB;

    /** Datos del tenant recién registrado que hacen falta para las peticiones cruzadas. */
    private record Tenant(long companyId, long userId, String token, String username) { }

    @BeforeAll
    void registrarLosDosTenants() throws Exception {
        limpiarDatosDePrueba(); // por si una ejecución anterior se interrumpió a medias
        a = registrar("Aislamiento Tenant A", RUC_A, EMAIL_A);
        b = registrar("Aislamiento Tenant B", RUC_B, EMAIL_B);
        assertThat(a.companyId()).isNotEqualTo(b.companyId());

        // Un vendedor REAL en cada empresa. Sin esto el caso del listado de vendedores pasaría por
        // vacuidad: con la tabla `vendedor` vacía, un findAll() sin acotar también devuelve [].
        crearVendedor(a);
        vendedorIdB = crearVendedor(b);
    }

    /** Crea el perfil de vendedor del admin del tenant y devuelve su id. */
    private long crearVendedor(Tenant t) throws Exception {
        MvcResult res = mockMvc.perform(post("/users/api/v1/vendedores/users/" + t.userId())
                        .header("Authorization", "Bearer " + t.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dniRuc\":\"70000001\",\"telefonoContacto\":\"999000111\"}"))
                .andReturn();

        assertThat(res.getResponse().getStatus())
                .as("el alta de vendedor debe funcionar dentro de la propia empresa. Cuerpo: %s",
                        res.getResponse().getContentAsString())
                .isEqualTo(201);

        return objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }

    private Tenant registrar(String nombre, String ruc, String email) throws Exception {
        String body = """
                {
                  "companyName": "%s",
                  "ruc": "%s",
                  "adminEmail": "%s",
                  "adminPassword": "Aislamiento2026",
                  "adminNombres": "Admin",
                  "adminApellidos": "Aislamiento",
                  "planCode": "STARTER"
                }
                """.formatted(nombre, ruc, email);

        MvcResult res = mockMvc.perform(post("/users/api/saas/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();

        assertThat(res.getResponse().getStatus())
                .as("el alta autoservicio debe funcionar; sin ella no hay test de aislamiento posible. Cuerpo: %s",
                        res.getResponse().getContentAsString())
                .isEqualTo(201);

        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return new Tenant(
                json.get("companyId").asLong(),
                json.get("userId").asLong(),
                json.get("token").asText(),
                json.get("username").asText());
    }

    // ---------------------------------------------------------------- control sano

    @Test
    @Order(1)
    @DisplayName("control: el ADMIN de A sí lee su propia empresa (el aislamiento no puede romper lo legítimo)")
    void controlSanoLeeSuPropiaEmpresa() throws Exception {
        int status = mockMvc.perform(get("/users/api/companies/" + a.companyId())
                        .header("Authorization", "Bearer " + a.token()))
                .andReturn().getResponse().getStatus();

        assertThat(status)
                .as("un ADMIN debe poder leer su propia empresa")
                .isEqualTo(200);
    }

    // ---------------------------------------------------------------- lectura cruzada

    @Test
    @Order(2)
    @DisplayName("A no puede leer la empresa de B")
    void noLeeLaEmpresaAjena() throws Exception {
        MvcResult res = mockMvc.perform(get("/users/api/companies/" + b.companyId())
                        .header("Authorization", "Bearer " + a.token()))
                .andReturn();

        assertThat(res.getResponse().getStatus())
                .as("GET /companies/{id} de otro tenant debe denegarse. Cuerpo devuelto: %s",
                        res.getResponse().getContentAsString())
                .isIn(403, 404);
    }

    @Test
    @Order(3)
    @DisplayName("A no puede leer el usuario de B por id")
    void noLeeElUsuarioAjenoPorId() throws Exception {
        MvcResult res = mockMvc.perform(get("/users/api/users/" + b.userId())
                        .header("Authorization", "Bearer " + a.token()))
                .andReturn();

        assertThat(res.getResponse().getStatus())
                .as("GET /users/{id} de otro tenant expone PII (documento, fecha de nacimiento). Cuerpo: %s",
                        res.getResponse().getContentAsString())
                .isIn(403, 404);
    }

    @Test
    @Order(4)
    @DisplayName("A no puede leer el usuario de B por username")
    void noLeeElUsuarioAjenoPorUsername() throws Exception {
        MvcResult res = mockMvc.perform(get("/users/api/users/username/" + b.username())
                        .header("Authorization", "Bearer " + a.token()))
                .andReturn();

        assertThat(res.getResponse().getStatus())
                .as("GET /users/username/{u} sirve de oráculo de existencia de cuentas. Cuerpo: %s",
                        res.getResponse().getContentAsString())
                .isIn(403, 404);
    }

    @Test
    @Order(5)
    @DisplayName("A no puede enumerar las empresas y roles de un usuario de B")
    void noEnumeraLasMembresiasAjenas() throws Exception {
        MvcResult res = mockMvc.perform(get("/users/api/user-companies/user/" + b.userId())
                        .header("Authorization", "Bearer " + a.token()))
                .andReturn();

        assertThat(res.getResponse().getStatus())
                .as("GET /user-companies/user/{id} revela companyId y roleIds de cualquier usuario. Cuerpo: %s",
                        res.getResponse().getContentAsString())
                .isIn(403, 404);
    }

    @Test
    @Order(6)
    @DisplayName("el listado de vendedores de A no incluye los de B (con un vendedor real en cada empresa)")
    void noListaVendedoresDeTodaLaPlataforma() throws Exception {
        // Precondición del test: debe existir al menos un vendedor de OTRA empresa, o el caso
        // pasaría por vacuidad. Antes de la migración V37 este test pasaba solo por eso.
        Long ajenos = jdbc.queryForObject(
                "select count(*) from dbshopusuarios.vendedor where company_id <> ?", Long.class, a.companyId());
        assertThat(ajenos).as("el fixture debe haber creado un vendedor en otra empresa").isPositive();

        MvcResult res = mockMvc.perform(get("/users/api/v1/vendedores")
                        .header("Authorization", "Bearer " + a.token()))
                .andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(200);

        String cuerpo = res.getResponse().getContentAsString();
        long propios = jdbc.queryForObject(
                "select count(*) from dbshopusuarios.vendedor where company_id = ?", Long.class, a.companyId());

        assertThat(objectMapper.readTree(cuerpo).size())
                .as("el listado debe traer solo los %d vendedores de la empresa propia. Cuerpo: %s", propios, cuerpo)
                .isEqualTo((int) propios);
        assertThat(cuerpo)
                .as("no debe filtrarse el id del vendedor de la otra empresa")
                .doesNotContain("\"id\":" + vendedorIdB + ",");
    }

    @Test
    @Order(9)
    @DisplayName("A no puede leer ni aprobar el vendedor de B")
    void noAlcanzaElVendedorAjeno() throws Exception {
        int lectura = mockMvc.perform(get("/users/api/v1/vendedores/" + vendedorIdB)
                        .header("Authorization", "Bearer " + a.token()))
                .andReturn().getResponse().getStatus();
        assertThat(lectura).as("GET /vendedores/{id} de otra empresa").isIn(403, 404);

        String estadoAntes = jdbc.queryForObject(
                "select estado_aprobacion from dbshopusuarios.vendedor where id = ?", String.class, vendedorIdB);

        mockMvc.perform(patch("/users/api/v1/vendedores/" + vendedorIdB + "/status")
                .header("Authorization", "Bearer " + a.token())
                .param("status", "APPROVED"));

        String estadoDespues = jdbc.queryForObject(
                "select estado_aprobacion from dbshopusuarios.vendedor where id = ?", String.class, vendedorIdB);

        assertThat(estadoDespues)
                .as("aprobar o rechazar el vendedor de otra empresa no puede surtir efecto")
                .isEqualTo(estadoAntes);
    }

    // ---------------------------------------------------------------- escritura cruzada

    @Test
    @Order(7)
    @DisplayName("A no puede modificar la empresa de B (verificado en base de datos)")
    void noModificaLaEmpresaAjena() throws Exception {
        String nombreOriginal = jdbc.queryForObject(
                "select name from dbshopusuarios.company where id = ?", String.class, b.companyId());

        String body = """
                {
                  "name": "SECUESTRADA POR EL TENANT A",
                  "ruc": "%s",
                  "domain": "secuestrado.example.com"
                }
                """.formatted(RUC_B);

        mockMvc.perform(put("/users/api/companies/" + b.companyId())
                .header("Authorization", "Bearer " + a.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        String nombreAhora = jdbc.queryForObject(
                "select name from dbshopusuarios.company where id = ?", String.class, b.companyId());

        assertThat(nombreAhora)
                .as("el nombre de la empresa de otro tenant NO puede cambiar; `domain` es además "
                        + "el campo con el que se resuelve el tenant en el checkout de invitado")
                .isEqualTo(nombreOriginal);
    }

    @Test
    @Order(8)
    @DisplayName("A no puede borrar al usuario de B (verificado en base de datos)")
    void noBorraElUsuarioAjeno() throws Exception {
        mockMvc.perform(delete("/users/api/users/" + b.userId())
                .header("Authorization", "Bearer " + a.token()));

        Long sigueExistiendo = jdbc.queryForObject(
                "select count(*) from dbshopusuarios.usuario where id = ?", Long.class, b.userId());

        assertThat(sigueExistiendo)
                .as("DELETE /users/{id} es un borrado REAL (deleteById), no una baja lógica: "
                        + "un ADMIN ajeno no puede hacer desaparecer al administrador de otra empresa")
                .isEqualTo(1L);
    }

    @Test
    @Order(10)
    @DisplayName("B09: un ADMIN de tenant no puede cambiar IGV_RATE, que es global para toda la plataforma")
    void noCambiaUnParametroGlobalDeLaPlataforma() throws Exception {
        String igvOriginal = jdbc.queryForObject(
                "select param_value from dbshopusuarios.erp_parameters where param_key = 'IGV_RATE' and tenant_id is null",
                String.class);

        mockMvc.perform(put("/users/api/system/parameters/IGV_RATE")
                .header("Authorization", "Bearer " + a.token())
                .contentType(MediaType.TEXT_PLAIN)
                .content("0.99"));

        String igvAhora = jdbc.queryForObject(
                "select param_value from dbshopusuarios.erp_parameters where param_key = 'IGV_RATE' and tenant_id is null",
                String.class);

        assertThat(igvAhora)
                .as("IGV_RATE es la tasa nacional de IGV y la fila es GLOBAL (tenant_id IS NULL): "
                        + "que un ADMIN de una empresa la cambie afecta la facturación de TODOS los "
                        + "tenants de la plataforma. Solo SUPERADMIN puede escribirla.")
                .isEqualTo(igvOriginal);
    }

    // ---------------------------------------------------------------- limpieza

    @AfterAll
    void limpiarDatosDePrueba() {
        // Orden impuesto por las claves foráneas hacia usuario/persona/company.
        // Se localiza por username y RUC de prueba (no por los ids capturados) para que la
        // limpieza funcione aunque un test destructivo haya alterado el estado.
        for (String username : List.of(EMAIL_A, EMAIL_B)) {
            Long usuarioId = unicoLong("select id from dbshopusuarios.usuario where username = ?", username);
            if (usuarioId == null) continue;
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
        for (String ruc : List.of(RUC_A, RUC_B)) {
            Long companyId = unicoLong("select id from dbshopusuarios.company where ruc = ?", ruc);
            if (companyId == null) continue;
            jdbc.update("delete from dbshopusuarios.user_company where company_id = ?", companyId);
            jdbc.update("delete from dbshopusuarios.company_module where company_id = ?", companyId);
            jdbc.update("delete from dbshopusuarios.company_rubro where company_id = ?", companyId);
            jdbc.update("delete from dbshopusuarios.saas_subscription where company_id = ?", companyId);
            jdbc.update("delete from dbshopusuarios.user_company_aud where company_id = ?", companyId);
            jdbc.update("delete from dbshopusuarios.company_aud where id = ?", companyId);
            jdbc.update("delete from dbshopusuarios.company where id = ?", companyId);
        }
    }

    private Long unicoLong(String sql, Object arg) {
        List<Long> filas = jdbc.queryForList(sql, Long.class, arg);
        return filas.isEmpty() ? null : filas.get(0);
    }
}
