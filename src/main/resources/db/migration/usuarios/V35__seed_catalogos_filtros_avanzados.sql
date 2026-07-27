-- Catálogos nuevos para los filtros avanzados del ERP (selects de estado/tipo/categoría
-- en las toolbars de listado). Misma convención que V23–V30:
--   param_group = 'CATALOGO'
--   param_key   = 'CATALOGO.<TABLA>.<CODIGO>'
--   param_value = etiqueta visible (español)
-- Servidos por GET /users/api/system/parameters/catalog/{tabla} y consumidos desde Angular
-- con catalogFilter(...) — reemplazan los <option> hardcodeados.
--
-- Los códigos salieron de los enums/entidades REALES de cada servicio (no son inventados).
-- Catálogos deliberadamente NO sembrados porque su valor no se persiste en ninguna columna
-- (se derivan en tiempo de consulta) o porque tendrían una sola opción:
--   * ESTADO_CPE: duplicado exacto de ESTADO_CPE_SUNAT (mismo enum PedidoEntity.EstadoCpe)
--   * ESTADO_CUPON_CLIENTE: NO persistido: se calcula en el controller desde usedAt/fecha fin -> un WHERE no puede filtrarlo
--   * ESTADO_PRESUPUESTO_COMPRAS: un unico valor real (ACTIVO) -> dropdown inutil
--   * ESTADO_USUARIO: la columna real es boolean is_active -> se filtra con staticFilter(activo true/false), no con catalogo
--   * SITUACION_STOCK_REORDEN: no existe columna persistida (requiereReorden() es booleano computado)
--   * TIPO_CONTRATO_PROVEEDOR: un unico valor real (MARCO) -> dropdown inutil
--   * ESTADO_STOCK: sin columna persistida en el modelo actual (verificado por grep)
--   * ESTADO_VENCIMIENTO_LOTE: sin columna persistida en el modelo actual (verificado por grep)
--   * TIPO_OPERACION_PLE: sin columna persistida en el modelo actual (verificado por grep)
--   * NIVEL_PUESTO: sin columna persistida en el modelo actual (verificado por grep)
--   * TIPO_ENVIO_STOREFRONT: sin columna persistida en el modelo actual (verificado por grep)

INSERT INTO erp_parameters (tenant_id, param_group, param_key, param_value, param_description, is_active, fecha_creacion, usuario_creacion, fecha_modificacion, usuario_modificacion, activo, editable, tipo)
VALUES
  -- ACCION_AUDITORIA  (Columna String libre (sin enum) en AuditLogContableEntity.java:28-29 ('accion', length=30))
  (NULL, 'CATALOGO', 'CATALOGO.ACCION_AUDITORIA.CREAR', 'Crear', 'Acción registrada en audit_log_contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ACCION_AUDITORIA.CERRAR', 'Cerrar', 'Acción registrada en audit_log_contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ACCION_AUDITORIA.REABRIR', 'Reabrir', 'Acción registrada en audit_log_contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- CLASE_ABC  (microshoplogistica/src/main/java/com/microshop/invalmacen/application/query/AbcAnalysisQueryService.java:)
  (NULL, 'CATALOGO', 'CATALOGO.CLASE_ABC.A', 'Clase A (alto valor)', 'Clasificación ABC de Pareto de un producto por valor de consumo', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.CLASE_ABC.B', 'Clase B (valor medio)', 'Clasificación ABC de Pareto de un producto por valor de consumo', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.CLASE_ABC.C', 'Clase C (bajo valor)', 'Clasificación ABC de Pareto de un producto por valor de consumo', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ENTIDAD_AUDITORIA_CONTABLE  (Columna String libre (sin enum) en AuditLogContableEntity.java:22-23 ('entidad_tipo', length=50))
  (NULL, 'CATALOGO', 'CATALOGO.ENTIDAD_AUDITORIA_CONTABLE.AsientoContable', 'Asiento contable', 'Tipo de entidad auditada en audit_log_contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ENTIDAD_AUDITORIA_CONTABLE.PeriodoContable', 'Periodo contable', 'Tipo de entidad auditada en audit_log_contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_ACTIVO_INACTIVO  (microshopcompras/src/main/java/com/microshop/compras/infrastructure/persistence/entity/ProveedorEntity.ja)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_ACTIVO_INACTIVO.ACTIVO', 'Activo', 'Estado activo/inactivo genérico (columna String, no boolean)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_ACTIVO_INACTIVO.INACTIVO', 'Inactivo', 'Estado activo/inactivo genérico (columna String, no boolean)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_ASIENTO_CONTABLE  (microshopcontabilidad/src/main/java/com/microshop/contabilidad/infrastructure/persistence/entity/AsientoC)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_ASIENTO_CONTABLE.BORRADOR', 'Borrador', 'Estado del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_ASIENTO_CONTABLE.CONAFECTAR', 'Con afectación pendiente', 'Estado del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_ASIENTO_CONTABLE.DEFINITIVO', 'Definitivo', 'Estado del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_ASIENTO_CONTABLE.CERRADO', 'Cerrado', 'Estado del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_ASIENTO_CONTABLE.ANULADO', 'Anulado', 'Estado del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_CIVIL  (src/main/java/com/microshop/rrhh/domain/model/Employee.java:257-263 (enum MaritalStatus, @Enumerated(Enum)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CIVIL.SOLTERO', 'Soltero', 'Estado civil', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CIVIL.CASADO', 'Casado', 'Estado civil', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CIVIL.DIVORCIADO', 'Divorciado', 'Estado civil', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CIVIL.VIUDO', 'Viudo', 'Estado civil', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CIVIL.CONVIVIENTE', 'Conviviente', 'Estado civil', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_CONCILIACION_BANCARIA  (Columna String libre en ConciliacionBancariaEntity.java:58-59,69 (default "BORRADOR" en @PrePersist))
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CONCILIACION_BANCARIA.BORRADOR', 'Borrador', 'Estado de la conciliación bancaria', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CONCILIACION_BANCARIA.CONCILIADO', 'Conciliado', 'Estado de la conciliación bancaria', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_CONSOLIDACION  (microshopcompras/src/main/java/com/microshop/compras/infrastructure/persistence/entity/SolicitudConsolida)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CONSOLIDACION.ABIERTA', 'Abierta', 'Estado de la consolidación de compras multitienda', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CONSOLIDACION.CERRADA', 'Cerrada', 'Estado de la consolidación de compras multitienda', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_CONVERSACION_CHAT  (src/main/java/com/microshop/users/infrastructure/persistence/entity/ChatConversacionEntity.java:35-38 (co)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CONVERSACION_CHAT.ABIERTA', 'Abierta', 'Estado conversacion chat', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CONVERSACION_CHAT.CERRADA', 'Cerrada', 'Estado conversacion chat', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_CPE_SUNAT  (microshopventas/src/main/java/com/microshop/ventas/infrastructure/persistence/entity/PedidoEntity.java:82)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CPE_SUNAT.PENDIENTE', 'Pendiente', 'Estado de emisión del comprobante de pago electrónico (CPE) ante SUNAT', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CPE_SUNAT.EMITIDO', 'Emitido', 'Estado de emisión del comprobante de pago electrónico (CPE) ante SUNAT', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_CPE_SUNAT.ERROR', 'Error', 'Estado de emisión del comprobante de pago electrónico (CPE) ante SUNAT', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_DECLARACION_TRIBUTARIA  (microshopcontabilidad/src/main/java/com/microshop/contabilidad/infrastructure/persistence/entity/Declarac)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_DECLARACION_TRIBUTARIA.BORRADOR', 'Borrador', 'Estado de la declaración tributaria (PDT 621)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_DECLARACION_TRIBUTARIA.PRESENTADA', 'Presentada', 'Estado de la declaración tributaria (PDT 621)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_DECLARACION_TRIBUTARIA.RECTIFICADA', 'Rectificada', 'Estado de la declaración tributaria (PDT 621)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_INTENTO_PAGO  (microshopventas/src/main/java/com/microshop/ventas/infrastructure/persistence/entity/PaymentAttemptEntity)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_INTENTO_PAGO.PENDING', 'Pendiente', 'Estado de un intento de pago contra MercadoPago', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_INTENTO_PAGO.APPROVED', 'Aprobado', 'Estado de un intento de pago contra MercadoPago', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_INTENTO_PAGO.REJECTED', 'Rechazado', 'Estado de un intento de pago contra MercadoPago', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_INTENTO_PAGO.CANCELLED', 'Cancelado', 'Estado de un intento de pago contra MercadoPago', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_META  (src/main/java/com/microshop/rrhh/domain/model/Goal.java:104-109 (enum GoalStatus, @Enumerated(EnumType.ST)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_META.EN_PROGRESO', 'En progreso', 'Estado meta', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_META.COMPLETADO', 'Completado', 'Estado meta', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_META.CANCELADO', 'Cancelado', 'Estado meta', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_META.RETRASADO', 'Retrasado', 'Estado meta', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_PEDIDO  (microshopventas/src/main/java/com/microshop/ventas/domain/valueobject/EstadoPedido.java:12-50 (sealed int)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PEDIDO.PENDIENTE', 'Pendiente', 'Estado del pedido online', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PEDIDO.PAGADO', 'Pagado', 'Estado del pedido online', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PEDIDO.ENVIADO', 'Enviado', 'Estado del pedido online', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PEDIDO.ENTREGADO', 'Entregado', 'Estado del pedido online', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PEDIDO.CANCELADO', 'Cancelado', 'Estado del pedido online', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_PICKING  (microshoplogistica/src/main/java/com/microshop/logistica/domain/enums/PickingStatus.java:3-8 + microshopl)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PICKING.PENDING_PICKING', 'Pendiente de picking', 'Estado de una orden/batch de picking', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PICKING.PICKING', 'En picking', 'Estado de una orden/batch de picking', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PICKING.PICKED', 'Recogido', 'Estado de una orden/batch de picking', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PICKING.CANCELLED', 'Cancelado', 'Estado de una orden/batch de picking', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_PLANILLA  (src/main/java/com/microshop/rrhh/domain/model/Payroll.java:180-185 (enum PayrollStatus, @Enumerated(EnumT)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PLANILLA.GENERADO', 'Generado', 'Estado planilla', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PLANILLA.APROBADO', 'Aprobado', 'Estado planilla', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PLANILLA.PAGADO', 'Pagado', 'Estado planilla', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PLANILLA.CANCELADO', 'Cancelado', 'Estado planilla', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_PRESUPUESTO  (Columna String libre en PresupuestoContableEntity.java:35-36 (default "BORRADOR"))
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PRESUPUESTO.BORRADOR', 'Borrador', 'Estado del presupuesto contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_PRESUPUESTO.APROBADO', 'Aprobado', 'Estado del presupuesto contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_REGLA_REPOSICION  (microshoplogistica/src/main/java/com/microshop/logistica/infrastructure/persistence/entity/ReplenishmentR)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_REGLA_REPOSICION.ACTIVE', 'Activa', 'Estado de una regla de reposición automática de stock', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_REGLA_REPOSICION.INACTIVE', 'Inactiva', 'Estado de una regla de reposición automática de stock', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_REGLA_REPOSICION.PAUSED', 'Pausada', 'Estado de una regla de reposición automática de stock', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_RESERVA_STOCK  (microshoplogistica/src/main/java/com/microshop/logistica/domain/enums/ReservationStatus.java:6-15 + micro)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_RESERVA_STOCK.RESERVED', 'Reservado', 'Estado del ciclo de vida de una reserva de stock', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_RESERVA_STOCK.RELEASED', 'Liberado', 'Estado del ciclo de vida de una reserva de stock', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_RESERVA_STOCK.CONSUMED', 'Consumido', 'Estado del ciclo de vida de una reserva de stock', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_RESERVA_STOCK.EXPIRED', 'Expirado', 'Estado del ciclo de vida de una reserva de stock', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_RUTA_ENTREGA  (microshoplogistica/src/main/java/com/microshop/logistica/domain/enums/RouteStatus.java:6-15 + microshoplo)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_RUTA_ENTREGA.PLANNED', 'Planificada', 'Estado de una ruta de entrega (last-mile)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_RUTA_ENTREGA.IN_PROGRESS', 'En progreso', 'Estado de una ruta de entrega (last-mile)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_RUTA_ENTREGA.COMPLETED', 'Completada', 'Estado de una ruta de entrega (last-mile)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_RUTA_ENTREGA.CANCELLED', 'Cancelada', 'Estado de una ruta de entrega (last-mile)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_SUSCRIPCION_SAAS  (src/main/java/com/microshop/users/infrastructure/persistence/entity/SaasSubscriptionEntity.java:26-28 (co)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_SUSCRIPCION_SAAS.TRIAL', 'Prueba (trial)', 'Estado suscripcion saas', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_SUSCRIPCION_SAAS.ACTIVE', 'Activa', 'Estado suscripcion saas', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_VALIDACION_SUNAT  (microshopcompras/src/main/java/com/microshop/compras/infrastructure/persistence/entity/FacturaProveedorEn)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VALIDACION_SUNAT.PENDIENTE', 'Pendiente', 'Estado de validación SUNAT de la factura de proveedor (simulado)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VALIDACION_SUNAT.ACEPTADA', 'Aceptada', 'Estado de validación SUNAT de la factura de proveedor (simulado)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VALIDACION_SUNAT.RECHAZADA', 'Rechazada', 'Estado de validación SUNAT de la factura de proveedor (simulado)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_VENTA_POS  (microshopventas/src/main/java/com/microshop/ventas/infrastructure/persistence/entity/VentaPosEntity.java:)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VENTA_POS.COMPLETADA', 'Completada', 'Estado de una venta de punto de venta (POS)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VENTA_POS.ANULADA', 'Anulada', 'Estado de una venta de punto de venta (POS)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ESTADO_VIGENCIA_COTIZACION  (microshopcompras/src/main/java/com/microshop/compras/application/command/CotizacionCommandService.java:61)
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VIGENCIA_COTIZACION.CREADA', 'Creada', 'Estado/vigencia de la cotización (RFQ)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VIGENCIA_COTIZACION.ENVIADA', 'Enviada', 'Estado/vigencia de la cotización (RFQ)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VIGENCIA_COTIZACION.EN_RESPUESTA', 'En respuesta', 'Estado/vigencia de la cotización (RFQ)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VIGENCIA_COTIZACION.ADJUDICADA', 'Adjudicada', 'Estado/vigencia de la cotización (RFQ)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VIGENCIA_COTIZACION.CONVERTIDA_OC', 'Convertida a orden de compra', 'Estado/vigencia de la cotización (RFQ)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ESTADO_VIGENCIA_COTIZACION.CANCELADA', 'Cancelada', 'Estado/vigencia de la cotización (RFQ)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- GENERO  (src/main/java/com/microshop/rrhh/domain/model/Employee.java:251-255 (enum Gender, @Enumerated(EnumType.ST)
  (NULL, 'CATALOGO', 'CATALOGO.GENERO.MASCULINO', 'Masculino', 'Genero', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.GENERO.FEMENINO', 'Femenino', 'Genero', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.GENERO.OTRO', 'Otro', 'Genero', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- METRICA_SLA_TRANSPORTISTA  (app-shop/src/app/features/logistica/models/carrier-sla.model.ts:103-110 (CARRIER_SLA_METRICS) — backend: )
  (NULL, 'CATALOGO', 'CATALOGO.METRICA_SLA_TRANSPORTISTA.ON_TIME_RATE', 'Tasa de puntualidad', 'Métrica de SLA de un transportista', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.METRICA_SLA_TRANSPORTISTA.AVG_DELIVERY_HOURS', 'Horas promedio de entrega', 'Métrica de SLA de un transportista', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.METRICA_SLA_TRANSPORTISTA.DAMAGE_RATE', 'Tasa de daños', 'Métrica de SLA de un transportista', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.METRICA_SLA_TRANSPORTISTA.FAILURE_RATE', 'Tasa de fallos de entrega', 'Métrica de SLA de un transportista', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.METRICA_SLA_TRANSPORTISTA.COST_PER_SHIPMENT', 'Costo por envío', 'Métrica de SLA de un transportista', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- MOTIVO_DEVOLUCION_POS  (app-shop/src/app/features/pos/pages/pos-devoluciones/pos-devoluciones.component.ts:8-16 (const MOTIVOS, t)
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_DEVOLUCION_POS.PRODUCTO_DEFECTUOSO', 'Producto defectuoso', 'Motivo de una devolución/nota de crédito de venta POS', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_DEVOLUCION_POS.PRODUCTO_INCORRECTO', 'Producto incorrecto o no solicitado', 'Motivo de una devolución/nota de crédito de venta POS', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_DEVOLUCION_POS.CAMBIO_OPINION', 'Cambio de opinión del cliente', 'Motivo de una devolución/nota de crédito de venta POS', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_DEVOLUCION_POS.ERROR_COBRO', 'Error en el cobro', 'Motivo de una devolución/nota de crédito de venta POS', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.MOTIVO_DEVOLUCION_POS.OTRO', 'Otro motivo', 'Motivo de una devolución/nota de crédito de venta POS', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- NIVEL_ALERTA  (microshopcompras/src/main/java/com/microshop/compras/infrastructure/persistence/entity/AlertaComprasEntit)
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_ALERTA.INFO', 'Informativo', 'Nivel de severidad de la alerta de compras', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_ALERTA.WARNING', 'Advertencia', 'Nivel de severidad de la alerta de compras', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_ALERTA.CRITICAL', 'Crítico', 'Nivel de severidad de la alerta de compras', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- NIVEL_CUENTA_PCGE  (microshopcontabilidad/src/main/java/com/microshop/contabilidad/infrastructure/persistence/entity/CuentaCo)
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_CUENTA_PCGE.1', 'Nivel 1', 'Nivel jerárquico de la cuenta contable PCGE', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_CUENTA_PCGE.2', 'Nivel 2', 'Nivel jerárquico de la cuenta contable PCGE', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_CUENTA_PCGE.3', 'Nivel 3', 'Nivel jerárquico de la cuenta contable PCGE', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_CUENTA_PCGE.4', 'Nivel 4', 'Nivel jerárquico de la cuenta contable PCGE', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_CUENTA_PCGE.5', 'Nivel 5', 'Nivel jerárquico de la cuenta contable PCGE', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- NIVEL_PROVEEDOR  (microshopcompras/src/main/java/com/microshop/compras/application/command/EvaluacionCommandService.java:89)
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_PROVEEDOR.EXCELENTE', 'Excelente', 'Nivel de desempeño del proveedor calculado por evaluaciones', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_PROVEEDOR.BUENO', 'Bueno', 'Nivel de desempeño del proveedor calculado por evaluaciones', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_PROVEEDOR.REGULAR', 'Regular', 'Nivel de desempeño del proveedor calculado por evaluaciones', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.NIVEL_PROVEEDOR.DEFICIENTE', 'Deficiente', 'Nivel de desempeño del proveedor calculado por evaluaciones', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- ORIGEN_ASIENTO_CONTABLE  (microshopcontabilidad/src/main/java/com/microshop/contabilidad/infrastructure/persistence/entity/AsientoC)
  (NULL, 'CATALOGO', 'CATALOGO.ORIGEN_ASIENTO_CONTABLE.VENTA', 'Venta', 'Origen del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ORIGEN_ASIENTO_CONTABLE.COMPRA', 'Compra', 'Origen del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ORIGEN_ASIENTO_CONTABLE.COSTO_VENTA', 'Costo de venta', 'Origen del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ORIGEN_ASIENTO_CONTABLE.TESORERIA', 'Tesorería', 'Origen del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ORIGEN_ASIENTO_CONTABLE.LOGISTICA', 'Logística', 'Origen del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ORIGEN_ASIENTO_CONTABLE.NOMINA', 'Nómina', 'Origen del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ORIGEN_ASIENTO_CONTABLE.CIERRE', 'Cierre', 'Origen del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.ORIGEN_ASIENTO_CONTABLE.MANUAL', 'Manual', 'Origen del asiento contable', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- PERIODO_MEDICION_SLA  (app-shop/src/app/features/logistica/models/carrier-sla.model.ts:113-117 (CARRIER_SLA_PERIODS) — backend: )
  (NULL, 'CATALOGO', 'CATALOGO.PERIODO_MEDICION_SLA.WEEKLY', 'Semanal', 'Período de medición de un SLA de transportista', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.PERIODO_MEDICION_SLA.MONTHLY', 'Mensual', 'Período de medición de un SLA de transportista', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.PERIODO_MEDICION_SLA.QUARTERLY', 'Trimestral', 'Período de medición de un SLA de transportista', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- PRIORIDAD_META  (src/main/java/com/microshop/rrhh/domain/model/Goal.java:111-115 (enum Priority, @Enumerated(EnumType.STRI)
  (NULL, 'CATALOGO', 'CATALOGO.PRIORIDAD_META.ALTA', 'Alta', 'Prioridad meta', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.PRIORIDAD_META.MEDIA', 'Media', 'Prioridad meta', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.PRIORIDAD_META.BAJA', 'Baja', 'Prioridad meta', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- REGIMEN_RENTA  (Columna String en DeclaracionTributariaEntity.java:52-54 ('regimen_renta'))
  (NULL, 'CATALOGO', 'CATALOGO.REGIMEN_RENTA.RMT', 'Régimen Mype Tributario (RMT)', 'Régimen tributario de renta de la declaración', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.REGIMEN_RENTA.MYPE', 'Régimen MYPE (pago directo)', 'Régimen tributario de renta de la declaración', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.REGIMEN_RENTA.GENERAL', 'Régimen General', 'Régimen tributario de renta de la declaración', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- TIPO_ALERTA_COMPRAS  (microshopcompras/src/main/java/com/microshop/compras/application/command/AlertaComprasCommandService.java)
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_ALERTA_COMPRAS.REORDEN', 'Reorden de stock', 'Tipo de alerta generada por el módulo de compras', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_ALERTA_COMPRAS.PRESUPUESTO_SOBREEJECUTADO', 'Presupuesto sobreejecutado', 'Tipo de alerta generada por el módulo de compras', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_ALERTA_COMPRAS.FACTURA_VENCIDA', 'Factura vencida', 'Tipo de alerta generada por el módulo de compras', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- TIPO_EVENTO_CONTABLE_LOGISTICA  (microshoplogistica/src/main/java/com/microshop/logistica/application/service/LogisticsAccountingService.j)
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_EVENTO_CONTABLE_LOGISTICA.SHIPMENT_COST', 'Costo de envío', 'Tipo de evento logístico mapeado a cuentas PCGE (asientos automáticos)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_EVENTO_CONTABLE_LOGISTICA.RETURN_REFUND', 'Reembolso por devolución', 'Tipo de evento logístico mapeado a cuentas PCGE (asientos automáticos)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_EVENTO_CONTABLE_LOGISTICA.INVENTORY_ADJUSTMENT', 'Ajuste de inventario', 'Tipo de evento logístico mapeado a cuentas PCGE (asientos automáticos)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- TIPO_FULFILLMENT  (microshoplogistica/src/main/java/com/microshop/logistica/domain/enums/FulfillmentType.java:7-20 + microsh)
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_FULFILLMENT.DELIVERY', 'Entrega a domicilio', 'Tipo de fulfillment de un envío (entrega/recojo/traslado)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_FULFILLMENT.STORE_PICKUP', 'Retiro en tienda', 'Tipo de fulfillment de un envío (entrega/recojo/traslado)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_FULFILLMENT.TRANSFER_TO_STORE', 'Traslado a tienda', 'Tipo de fulfillment de un envío (entrega/recojo/traslado)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_FULFILLMENT.SHIP_FROM_STORE', 'Despacho desde tienda', 'Tipo de fulfillment de un envío (entrega/recojo/traslado)', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- TIPO_MOVIMIENTO_CREDITO  (microshopusers/src/main/java/com/microshop/users/infrastructure/persistence/entity/CreditTransactionEntit)
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_MOVIMIENTO_CREDITO.RECARGA', 'Recarga', 'Tipo de movimiento de la cuenta de crédito del cliente', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_MOVIMIENTO_CREDITO.USO', 'Uso', 'Tipo de movimiento de la cuenta de crédito del cliente', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_MOVIMIENTO_CREDITO.DEVOLUCION', 'Devolución', 'Tipo de movimiento de la cuenta de crédito del cliente', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_MOVIMIENTO_CREDITO.BONUS', 'Bono', 'Tipo de movimiento de la cuenta de crédito del cliente', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_MOVIMIENTO_CREDITO.AJUSTE', 'Ajuste', 'Tipo de movimiento de la cuenta de crédito del cliente', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- TIPO_NOTIFICACION  (microshopusers/src/main/java/com/microshop/users/infrastructure/persistence/entity/NotificationEntity.jav)
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_NOTIFICACION.ORDER_STATUS_CHANGE', 'Cambio de estado de pedido', 'Tipo de notificación in-app del usuario', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_NOTIFICACION.REVIEW_REQUEST', 'Solicitud de reseña', 'Tipo de notificación in-app del usuario', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_NOTIFICACION.ABANDONED_CART', 'Carrito abandonado', 'Tipo de notificación in-app del usuario', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_NOTIFICACION.PROMO_OFFER', 'Oferta promocional', 'Tipo de notificación in-app del usuario', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_NOTIFICACION.SYSTEM', 'Sistema', 'Tipo de notificación in-app del usuario', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- TIPO_NOTIFICACION_LOGISTICA  (microshoplogistica/src/main/java/com/microshop/logistica/domain/enums/NotificationType.java:6-11 + micros)
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_NOTIFICACION_LOGISTICA.STOCK_BAJO', 'Stock bajo', 'Tipo de notificación in-app del módulo de logística', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_NOTIFICACION_LOGISTICA.ENVIO_RETRASADO', 'Envío retrasado', 'Tipo de notificación in-app del módulo de logística', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_NOTIFICACION_LOGISTICA.PICKING_PENDIENTE', 'Picking pendiente', 'Tipo de notificación in-app del módulo de logística', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_NOTIFICACION_LOGISTICA.DEVOLUCION_PENDIENTE', 'Devolución pendiente', 'Tipo de notificación in-app del módulo de logística', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_NOTIFICACION_LOGISTICA.GENERAL', 'General', 'Tipo de notificación in-app del módulo de logística', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- TIPO_REFERENCIA_ALERTA  (microshopcompras/src/main/java/com/microshop/compras/application/command/AlertaComprasCommandService.java)
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_REFERENCIA_ALERTA.PUNTO_REORDEN', 'Punto de reorden', 'Tipo de entidad referenciada por la alerta de compras', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_REFERENCIA_ALERTA.PRESUPUESTO', 'Presupuesto', 'Tipo de entidad referenciada por la alerta de compras', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_REFERENCIA_ALERTA.FACTURA', 'Factura', 'Tipo de entidad referenciada por la alerta de compras', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- TIPO_TRANSACCION_REGLA_ASIENTO  (Columna String libre VARCHAR(30) en ReglaAsientoAutomaticoEntity.java:37-38 ('tipo_transaccion'))
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_TRANSACCION_REGLA_ASIENTO.VENTA', 'Venta', 'Tipo de transacción origen de una regla de asiento automático', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_TRANSACCION_REGLA_ASIENTO.COMPRA', 'Compra', 'Tipo de transacción origen de una regla de asiento automático', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_TRANSACCION_REGLA_ASIENTO.NOMINA', 'Nómina', 'Tipo de transacción origen de una regla de asiento automático', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_TRANSACCION_REGLA_ASIENTO.TESORERIA', 'Tesorería', 'Tipo de transacción origen de una regla de asiento automático', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_TRANSACCION_REGLA_ASIENTO.INVENTARIO', 'Inventario', 'Tipo de transacción origen de una regla de asiento automático', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_TRANSACCION_REGLA_ASIENTO.LOGISTICA', 'Logística', 'Tipo de transacción origen de una regla de asiento automático', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- TIPO_VACACION  (src/main/java/com/microshop/rrhh/domain/model/VacationRequest.java:129-134 (enum VacationType, @Enumerate)
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_VACACION.ANUAL', 'Anual', 'Tipo vacacion', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_VACACION.TRUNCAS', 'Truncas', 'Tipo vacacion', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_VACACION.COMPENSATORIAS', 'Compensatorias', 'Tipo vacacion', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.TIPO_VACACION.SIN_GOCE', 'Sin goce', 'Tipo vacacion', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  -- UBIGEO_DEPARTAMENTO  (No hay tabla/enum/constantes de ubigeo en el código (columnas departamento/provincia/distrito son String )
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.AMAZONAS', 'Amazonas', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.ANCASH', 'Áncash', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.APURIMAC', 'Apurímac', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.AREQUIPA', 'Arequipa', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.AYACUCHO', 'Ayacucho', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.CAJAMARCA', 'Cajamarca', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.CALLAO', 'Callao', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.CUSCO', 'Cusco', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.HUANCAVELICA', 'Huancavelica', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.HUANUCO', 'Huánuco', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.ICA', 'Ica', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.JUNIN', 'Junín', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.LA_LIBERTAD', 'La Libertad', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.LAMBAYEQUE', 'Lambayeque', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.LIMA', 'Lima', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.LORETO', 'Loreto', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.MADRE_DE_DIOS', 'Madre de Dios', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.MOQUEGUA', 'Moquegua', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.PASCO', 'Pasco', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.PIURA', 'Piura', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.PUNO', 'Puno', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.SAN_MARTIN', 'San Martín', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.TACNA', 'Tacna', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.TUMBES', 'Tumbes', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog'),
  (NULL, 'CATALOGO', 'CATALOGO.UBIGEO_DEPARTAMENTO.UCAYALI', 'Ucayali', 'Ubigeo departamento', true, NOW(), 'SYSTEM', NOW(), 'SYSTEM', true, false, 'catalog')
ON CONFLICT (tenant_id, param_key) DO NOTHING;
