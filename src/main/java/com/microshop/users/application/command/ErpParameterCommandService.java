package com.microshop.users.application.command;

import com.microshop.users.config.security.SecurityContextUtils;
import com.microshop.users.infrastructure.persistence.repository.ErpParameterJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comandos de escritura sobre los parámetros ERP.
 *
 * <p><b>Corrección 2026-07-28 (B09).</b> El javadoc anterior decía «parámetros ERP editables por
 * empresa» y era FALSO en los dos extremos:</p>
 * <ul>
 *   <li>Estas filas son GLOBALES ({@code tenant_id IS NULL}) y {@link #updateParameter} escribía
 *       siempre esa fila global. Como el endpoint solo exigía rol ADMIN, un administrador de
 *       cualquier empresa que cambiara {@code IGV_RATE} lo cambiaba para TODOS los tenants de la
 *       plataforma: escritura cross-tenant silenciosa con impacto fiscal.</li>
 *   <li>No existe ninguna vía —ni UI ni API— para crear la fila de un tenant, y
 *       {@code ErpParameterQueryService.getForTenant(key, tenantId)} no tiene NI UN llamador: todas
 *       las lecturas van por {@code getGlobal}/{@code getAllActiveAsDto}. El sistema es global de
 *       punta a punta.</li>
 * </ul>
 *
 * <p>Los dos parámetros marcados {@code editable} lo confirman: {@code IGV_RATE} es la tasa
 * nacional de IGV del Perú y {@code SHOP_THEME_SEASONAL_ENABLED} es un interruptor de plataforma.
 * Ninguno es un valor «por empresa». Por eso la escritura queda reservada a SUPERADMIN (matcher en
 * {@code SecurityConfig} + esta comprobación como defensa en profundidad) en vez de convertir el
 * sistema en multi-tenant, que sería una feature nueva y no un arreglo.</p>
 *
 * <p>Si en el futuro hace falta configuración por empresa, el camino es: crear la fila con
 * {@code tenant_id}, hacer que las lecturas usen {@code getForTenant} con el companyId del JWT, y
 * recién entonces permitir la escritura a un ADMIN de tenant sobre SU fila.</p>
 */
@Service
@RequiredArgsConstructor
public class ErpParameterCommandService {

    private final ErpParameterJpaRepository repository;

    /** Resultado de la actualización, para que el controller mapee el código HTTP correspondiente. */
    public enum UpdateResult { NOT_FOUND, FORBIDDEN, OK }

    @Transactional
    public UpdateResult updateParameter(String key, String value) {
        // Defensa en profundidad: además del matcher de SecurityConfig, se comprueba aquí, para que
        // un futuro caller interno no reintroduzca la escritura cross-tenant sin darse cuenta.
        if (!SecurityContextUtils.isSuperAdmin()) {
            throw new AccessDeniedException(
                    "Los parámetros del sistema son configuración global de la plataforma: "
                            + "solo un SUPERADMIN puede modificarlos");
        }
        var paramOpt = repository.findByParamKeyAndTenantIdIsNull(key);
        if (paramOpt.isEmpty()) {
            return UpdateResult.NOT_FOUND;
        }
        var param = paramOpt.get();
        if (!Boolean.TRUE.equals(param.getEditable())) {
            return UpdateResult.FORBIDDEN;
        }
        param.setParamValue(value.trim().replace("\"", ""));
        repository.save(param);
        return UpdateResult.OK;
    }
}
