package com.microshop.users.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Petición para cambiar la empresa activa del usuario autenticado.
 *
 * <p>Antes el endpoint recibía un {@code Map<String, Long>} y hacía {@code body.get("targetCompanyId")}.
 * Si la clave no venía (o venía mal escrita), el valor era {@code null} y NADIE lo rechazaba:
 * {@code validateCompanyMembership} empieza con {@code if (companyId == null) return;}, así que la
 * petición seguía hasta {@code generateJwtToken(user, null)} y el servicio respondía <b>200 con un
 * JWT sin claim {@code companyId} ni claim {@code modules}</b>. Verificado en runtime el 2026-07-29.
 *
 * <p>Esa "sesión sin tenant" es un fallo por los dos extremos a la vez:
 * <ul>
 *   <li><b>fuga</b>: sin claim companyId, TenantFilter deja TenantContext vacío, y con él se activan
 *       todos los caminos fail-open del servicio de ventas (el catálogo devolvía las seis empresas);</li>
 *   <li><b>rotura</b>: sin claim modules, ModuloContratadoFilter no ve ningún módulo contratado, así
 *       que el usuario pierde el acceso a todo el ERP.</li>
 * </ul>
 *
 * <p>Con un record validado, Spring rechaza la petición con 400 antes de llegar al servicio. El
 * nombre del campo se mantiene ({@code targetCompanyId}) porque es el que envía el frontend
 * (menu-switch-account.component.ts:44) y cambiarlo rompería el cambio de empresa.
 */
public record SwitchCompanyRequest(
        @NotNull(message = "targetCompanyId es obligatorio")
        @Positive(message = "targetCompanyId debe ser un identificador positivo")
        Long targetCompanyId
) {}
