package com.microshop.users.infrastructure.rest.controller;

import com.microshop.users.application.command.SesionRevocacionService;
import com.microshop.users.infrastructure.persistence.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Lo que {@code microshopusers} publica sobre el <b>maestro de tenants</b> para que los otros cinco
 * servicios puedan converger. Endpoints internos (s2s), no de usuario final.
 *
 * <h2>Por qué existe: el maestro de empresa está duplicado y ha divergido</h2>
 * <p>Medido el 2026-07-29 y es el hallazgo más grave de esta ronda. La tabla {@code company} existe
 * <b>dos veces</b> —{@code dbshopusuarios.company} y {@code dbshopventas.company}—, cada una con su
 * propio CRUD y <b>sin ninguna sincronización</b> entre ellas. No es una copia desfasada: son datos
 * distintos para el mismo id.</p>
 *
 * <table border="1">
 *   <caption>Mismo id, dos negocios distintos</caption>
 *   <tr><th>id</th><th>en users</th><th>en ventas</th></tr>
 *   <tr><td>1</td><td>Microshop Default Company · RUC 20000000001</td><td>TechHub Store · RUC 20123456789</td></tr>
 *   <tr><td>2</td><td>MININTER · RUC 20131366966</td><td>Fashion Forward · RUC 20234567890</td></tr>
 *   <tr><td>4</td><td>MicroShop S.A.C. · RUC 20123456789</td><td>Beauty Boutique · RUC 20456789012</td></tr>
 *   <tr><td>6</td><td><b>no existe</b></td><td>Kids World</td></tr>
 *   <tr><td>7, 8</td><td>Empresa Test Fix, INNOVACION</td><td><b>no existen</b></td></tr>
 * </table>
 *
 * <p><b>Ni un RUC coincide.</b> Y la consecuencia no es cosmética: el {@code companyId} que este
 * servicio pone en el JWT se usa en ventas para acotar productos, pedidos, clientes y turnos de caja,
 * cuyas tablas tienen FK contra <b>la copia de ventas</b>. Verificado con una violación de FK real: el
 * tenant INNOVACION (id 8), que tiene usuario y membresía en users, <b>no puede crear un cliente en
 * ventas</b> —{@code fk_cliente_company}: la llave (company_id)=(8) no está presente en la tabla
 * company—, y lo mismo aplica a productos, pedidos y turnos. Ese tenant está funcionalmente fuera del
 * módulo de ventas. Al revés, quien opera como id 4 está trabajando sobre el catálogo de otro
 * negocio.</p>
 *
 * <p>Esto es lo que de verdad hay debajo de N02/N03/N05, que hablaban de tipos de columna
 * ({@code Long} vs {@code UUID} vs {@code String}). El tipo es lo de menos si la <b>identidad</b> del
 * tenant no es la misma entidad en cada servicio. Reconciliar los datos existentes NO se hace desde
 * aquí: cambiar el nombre o el RUC de una empresa en ventas reasigna la propiedad de sus productos y
 * pedidos, y eso es una decisión de negocio. Lo que sí se puede hacer, y es lo que hace esta clase, es
 * <b>publicar el maestro</b> para que la reconciliación sea posible y para que nadie más tenga que
 * adivinar cuál de las dos tablas manda: manda esta.</p>
 */
@RestController
@RequestMapping("/api/internal")
// Sin esto los dos endpoints caerian en `anyRequest().authenticated()`, y entonces CUALQUIER usuario
// autenticado de CUALQUIER tenant podria leer el maestro con el RUC y el nombre de todas las empresas
// de la plataforma. Es s2s (o soporte), no informacion de usuario final.
@PreAuthorize("hasRole('INTERNAL_SERVICE') or hasRole('SUPERADMIN')")
@RequiredArgsConstructor
@Slf4j
public class InternalTenantController {

    private final CompanyRepository companyRepository;
    private final SesionRevocacionService sesionRevocacionService;
    private final com.microshop.users.application.query.UserCompanyQueryService userCompanyQueryService;

    /**
     * Usuarios de una empresa con rol de administración, para que otro servicio pueda dirigirle una
     * notificación operativa <b>a esa empresa</b> y no a un destinatario adivinado.
     *
     * <p>Existe por un fallo concreto: el evaluador de alertas de stock de logística iteraba TODOS los
     * tenants y mandaba el detalle de inventario crítico de cada uno a un {@code userId} HARDCODEADO
     * (el 1, que sólo es miembro de dos de las seis empresas). O sea entregaba a un usuario el
     * inventario de empresas a las que no pertenece. Al quitar el hardcode la alerta quedó sin
     * destinatario y por tanto apagada; este endpoint es lo que la vuelve a encender <b>bien</b>.</p>
     *
     * <p>Devuelve sólo miembros ACTIVOS con rol ADMIN o SUPERADMIN de esa empresa. Lista vacía si la
     * empresa no tiene ninguno: el consumidor debe entonces NO notificar, nunca caer a un default.</p>
     */
    @GetMapping("/companies/{companyId}/admin-user-ids")
    public ResponseEntity<List<Long>> adminsDeEmpresa(@PathVariable Long companyId) {
        List<Long> ids = userCompanyQueryService.adminUserIdsDeEmpresa(companyId);
        log.debug("GET /api/internal/companies/{}/admin-user-ids -> {} destinatario(s)", companyId, ids.size());
        return ResponseEntity.ok(ids);
    }

    /**
     * Maestro de tenants. <b>Fuente de verdad</b> de la identidad de empresa del ERP.
     *
     * <p>Devuelve id, RUC, nombre, estado y dominio. Un consumidor que quiera reconciliar su propia
     * copia debe cruzar por <b>RUC</b>, no por id: el id coincide por casualidad de secuencias y es
     * justamente lo que ha divergido.</p>
     */
    @GetMapping("/companies")
    public ResponseEntity<List<Map<String, Object>>> maestroDeEmpresas() {
        List<Map<String, Object>> maestro = companyRepository.findAll().stream()
                .map(c -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<String, Object>();
                    m.put("id", c.getId());
                    m.put("ruc", c.getRuc());
                    m.put("name", c.getName());
                    m.put("active", c.isActive());
                    m.put("domain", c.getDomain());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(maestro);
    }

    /**
     * Estado de revocación de un token, por su {@code jti} (M27).
     *
     * <p>Es lo que un servicio consumidor necesita para que suspender un tenant corte de verdad a quien
     * ya está dentro, en vez de esperar a que el token expire. <b>Debe consultarse con caché de TTL
     * corto</b> (del orden de un minuto), no por petición: la ventana de exposición pasa de 24 h a ese
     * TTL, y el coste de una llamada por minuto y por tenant es despreciable. Hacerlo por petición
     * convertiría cada request del ERP en una llamada entre servicios.</p>
     *
     * <p>{@code revocada: false} para un {@code jti} desconocido — las sesiones anteriores a V40 no lo
     * tienen guardado y la firma ya se validó en el llamador, así que «desconocido» significa «token
     * legítimo antiguo». Ver {@code SesionRevocacionService.estaRevocado}.</p>
     */
    @GetMapping("/sesiones/{jti}/revocada")
    public ResponseEntity<Map<String, Object>> estaRevocada(@PathVariable String jti) {
        boolean revocada = sesionRevocacionService.estaRevocado(jti);
        return ResponseEntity.ok(Map.of("jti", jti, "revocada", revocada));
    }
}
