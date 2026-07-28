---
name: project-compras-frontend-uuid-inputs-fix
description: 2026-07-28 fix de 5 pantallas de app-shop/features/compras que exponían inputs de UUID libres en vez de selects server-side
metadata:
  type: project
---

Arreglados en `app-shop/src/app/features/compras/pages/`: evaluaciones (bug real — faltaba
control `periodo` en el FormGroup, backend `@NotNull` → el POST daba 400 SIEMPRE; + ordenCompraId
select + error inline en el drawer), solicitudes-compra (modal "Convertir a OC": proveedorId/
almacenDestino de texto libre → `app-server-search-select`), contratos (proveedorId en alta),
puntos-reorden (productoId en alta vía `app-product-lookup`; proveedorId en drawer "Editar
configuración"), consolidaciones (productoId vía lookup + storeId vía select de sucursales).

**Why:** inputs de UUID a mano son fuente de error humano y bloqueaban el flujo
solicitud-aprobada → orden-de-compra.

**Gotchas descubiertos (aplican a cualquier trabajo futuro en compras/inventory):**
- `ProductResponse` (`@core/models/product.model.ts`) NO tiene campo `sku`. `<app-product-lookup>`
  (ver `ordenes-compra.component.ts:472-480`) solo puede autocompletar `productoId`
  (via `productIdToUuid(product.id)`) y `productoNombre` — el SKU se queda de tecleo manual
  SIEMPRE que se use este componente. No asumir que "rellena los tres campos".
- No existe una entidad "Store"/sucursal en microshopcompras — `ConsolidacionTiendaEntity.storeId`
  es un `String(50)` libre, no un UUID FK. El select correcto para "sucursales del tenant" es
  `SucursalService.list(companyId)` en `features/admin/services/sucursal.service.ts` (devuelve
  `Sucursal[]` simple, sin paginar — pensado para selects). `Sucursal.id` es `number`; convertir a
  `String(id)` al guardarlo en `storeId`.
- GlobalExceptionHandler de microshopcompras usa `ProblemDetail` RFC 7807 → el mensaje útil viaja
  en `error.error?.detail` (no `.message`). El interceptor global `http-error.interceptor.ts` ya
  muestra un TOAST automático para 400/etc. con ese `detail` — pero si el formulario no tiene un
  signal de error inline (patrón `submitError` + `<app-alert>` dentro del drawer, ver
  `ordenes-compra.component.ts`), el usuario solo ve el toast y no un mensaje pegado al campo.
  Para nuevos drawers de compras: siempre agregar `submitError = signal<string|null>(null)` +
  `err.error?.detail ?? err.error?.message ?? 'fallback'` en el `error:` callback.
- `select-sources.ts` de compras es un archivo COMPARTIDO entre páginas — si dos agentes en
  paralelo (uno en evaluaciones, otro en recepcion/facturas-proveedor/devoluciones) necesitan la
  misma factory (`ordenCompraSelectSource`), ambos pueden añadirla a la vez → duplicate function
  implementation. Revisar el archivo actual con Read justo antes de escribir si hay otro agente
  trabajando en paralelo sobre el mismo feature.
