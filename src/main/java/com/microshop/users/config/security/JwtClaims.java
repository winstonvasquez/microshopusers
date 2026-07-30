package com.microshop.users.config.security;

/**
 * Contrato de los claims del JWT del ERP. <b>Fuente única de verdad.</b>
 *
 * <h2>Por qué existe</h2>
 * <p>{@code microshopusers} es el único servicio con la clave privada, así que es el único emisor de
 * tokens; los otros cinco solo validan con la clave pública. Pero el contrato de lo que va dentro del
 * token no estaba escrito en ninguna parte, y eso tuvo dos consecuencias medidas el 2026-07-29:</p>
 *
 * <ol>
 *   <li><b>Las dos rutas de emisión de este mismo servicio ya habían divergido.</b>
 *       {@code AuthCommandService.generateJwtToken} emitía {@code roles}, y
 *       {@code SaasOnboardingCommandService} no — de modo que un usuario que se registraba por el
 *       flujo SaaS recibía un token <b>sin roles</b> y todo {@code hasRole(...)} le fallaba en los seis
 *       servicios. Nadie lo había notado porque el registro y el login son caminos distintos.</li>
 *   <li><b>Los seis consumidores leen el token de tres formas distintas</b> (principal
 *       {@code JwtUserPrincipal} en compras y contabilidad, {@code Authentication.details} en users,
 *       ventas y analítica, y {@code TenantContext} en logística), y <b>ninguno leía
 *       {@code modules}</b>, que es la razón por la que el plan contratado no se validaba en ningún
 *       backend (M06).</li>
 * </ol>
 *
 * <p>Consolidar el aspecto de tenant en una librería compartida (D3) estaba bloqueado precisamente por
 * esa divergencia. Esta clase es la mitad rescatable de ese trabajo: fija el contrato en el emisor, que
 * es donde no puede haber discusión, y deja que cada consumidor converja a su ritmo sin necesitar
 * {@code microshopcommon} — que hoy es código muerto y no lo consume nadie.</p>
 *
 * <h2>Tipos EN EL CABLE, que es lo que se malinterpreta</h2>
 * <p>Los tipos de abajo son los que Jackson serializa dentro del token, no los del dominio. Importa
 * porque los consumidores tratan la empresa como {@code UUID} (compras, contabilidad, logística) y
 * aquí viaja como número: la conversión es el UUID sintético
 * {@code new UUID(0, companyId)}, y hacerla mal da 400, no lista vacía.</p>
 *
 * <table border="1">
 *   <caption>Claims emitidos</caption>
 *   <tr><th>Claim</th><th>Tipo en el cable</th><th>Obligatorio</th><th>Semántica</th></tr>
 *   <tr><td>{@code sub}</td><td>String</td><td>sí</td><td>{@code username} del usuario</td></tr>
 *   <tr><td>{@code jti}</td><td>String (UUIDv4)</td><td>sí</td><td>Identificador único del token.
 *       Sin él, dos logins del mismo usuario en el mismo segundo producían tokens byte-idénticos
 *       ({@code NumericDate} tiene precisión de segundo). Es la clave por la que se revoca.</td></tr>
 *   <tr><td>{@code userId}</td><td>Number (Long)</td><td>sí</td><td>Id del usuario</td></tr>
 *   <tr><td>{@code companyId}</td><td>Number (Long)</td><td>no*</td><td>Empresa ACTIVA de la sesión.
 *       Ausente solo si el usuario no tiene ninguna membresía. Los consumidores que usan UUID lo
 *       convierten con el UUID sintético.</td></tr>
 *   <tr><td>{@code roles}</td><td>Array&lt;String&gt;</td><td>sí</td><td>Authorities de Spring
 *       Security <b>con el prefijo {@code ROLE_}</b>, en mayúsculas. Sin el prefijo, ni
 *       {@code hasRole} ni {@code hasAuthority} reconocen nada.</td></tr>
 *   <tr><td>{@code modules}</td><td>String (CSV)</td><td>no**</td><td>Códigos de módulo del plan
 *       contratado, separados por coma: {@code POS,VENTAS,COMPRAS,INVENTARIO,CONTABILIDAD,LOGISTICA,
 *       TESORERIA,RRHH}. <b>Su ausencia significa «ningún módulo», no «todos»</b> — ver la nota de
 *       abajo, porque significaba lo contrario y era un agujero de negocio.</td></tr>
 * </table>
 *
 * <p>* {@code companyId} ausente = usuario sin empresa activa. No es un caso a ignorar: los endpoints
 * multi-tenant deben rechazarlo, no tratarlo como «cualquier empresa».</p>
 *
 * <p>** Hasta el 2026-07-29 {@code SaasQueryService.getEnabledModuleCodes} devolvía <b>todos</b> los
 * módulos activos cuando la empresa no tenía suscripción, así que un tenant sin plan recibía el
 * catálogo completo en el claim. Con el claim además sin validar en ningún backend, el plan contratado
 * no restringía nada por ninguno de los dos lados. Corregido: sin suscripción, sin módulos.</p>
 */
public final class JwtClaims {

    private JwtClaims() {
    }

    /** Identificador único del token ({@code jti} estándar). Clave de revocación. */
    public static final String JTI = "jti";

    /** Id del usuario, {@code Number} en el cable. */
    public static final String USER_ID = "userId";

    /** Id de la empresa activa, {@code Number} en el cable. Ausente = sin empresa activa. */
    public static final String COMPANY_ID = "companyId";

    /** Authorities con prefijo {@code ROLE_}, {@code Array<String>} en el cable. */
    public static final String ROLES = "roles";

    /** Códigos de módulo del plan, CSV. Ausente = ningún módulo habilitado. */
    public static final String MODULES = "modules";

    /**
     * Separador de {@link #MODULES}. Se documenta porque los consumidores tienen que partir por él;
     * si algún día se cambiara a un array JSON habría que tocar los seis servicios a la vez.
     */
    public static final String MODULES_SEPARATOR = ",";
}
