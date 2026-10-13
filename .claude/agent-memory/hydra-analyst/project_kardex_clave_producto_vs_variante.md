---
name: kardex-clave-producto-vs-variante
description: inventory_stock.product_id mezcla id de producto (compras, restock de devoluciones) e id de variante (ventas SALIDA_VENTA/STOCK_RETURN); 514/515 variantes tienen id distinto de su producto
metadata:
  type: project
---

El kardex de invalmacén (logística) no tiene una clave única de artículo: compras (ENTRADA_COMPRA) y el restock de inspección de devoluciones mandan el id de PRODUCTO; ventas (SALIDA_VENTA, STOCK_RETURN) manda el id de VARIANTE. Medido 2026-10-12: 514 de 515 variantes tienen `id ≠ producto_id`.

**Why:** cualquier flujo que entre por un camino y salga por otro mueve filas distintas de `inventory_stock` sin error.

**How to apply:** al revisar un movimiento de stock nuevo o un contrato s2s hacia `/inventory/api/inventory/movements`, comprobar qué id viaja en `productId` (LSB del UUID sintético) y compararlo con el camino inverso. Es decisión de arquitectura, no un fix local — escalarlo al orquestador. Relacionado: [[compensacion-stock-parcial-outbox]].

**Resuelto 2026-10-12 (ronda 3):** ventas manda ahora `productoId` en STOCK_DECREMENT/STOCK_RETURN y
suma por producto (`OutboxEventHandler.extractItems`); los eventos viejos con sólo `varianteId` se
traducen a su producto antes de despacharse. El kardex queda por id de PRODUCTO de punta a punta.
Lo que sigue valiendo: al tocar cualquier movimiento s2s hacia invalmacén, verificar que el id sea
el de producto.
