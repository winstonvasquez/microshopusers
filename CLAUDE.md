# microshopusers — CLAUDE.md

**Puerto:** 8080 | **Schemas DB:** `dbshopusuarios` + `dbshoprrhh` | **Sin context-path** (usa ApiPaths: `/users/api` y `/hr/api`)

## Responsabilidad

### Módulo Usuarios (`com.microshop.users`)
- Auth JWT RSA-256 (solo este servicio tiene la clave privada)
- Registro, login, perfil, sesiones
- Empresas (multi-tenancy por RUC, `CompanyEntity`)
- Roles y permisos
- Suscripciones SaaS
- Parámetros ERP (editables por empresa)
- Chat interno
- Crédito de clientes (credit accounts / transactions)
- Reseteo de contraseñas
- Notificaciones (tabla `notifications`)
- Preferencias de tema por usuario/empresa
- Segmentos de usuario

### Módulo RRHH (`com.microshop.rrhh`)
- Empleados, departamentos, cargos
- Planillas/nómina
- Vacaciones, asistencia
- Evaluaciones de desempeño 360°
- Capacitaciones
- Portal autoservicio

**NO implementar aquí:** lógica de ventas (→ microshopventas).

## Arquitectura multi-schema

- `FlywayConfig.java`: dos Flyway beans — `db/migration/usuarios/` y `db/migration/rrhh/`
- Entidades rrhh llevan `@Table(schema = "dbshoprrhh")`
- Entidades users usan schema `dbshopusuarios` (default datasource)
- Excepciones compartidas: `com.microshop.users.shared.exception` — usar en AMBOS módulos (NO crear excepciones en `rrhh/shared/exception/`)

## Migraciones Flyway

### Schema `dbshopusuarios` — última: V44  ·  medido 2026-08-06

| Versión | Contenido |
|---|---|
| V1 | Schema inicial: usuarios, empresas, roles |
| V2 | Vendedores |
| V3 | Seed admin inicial |
| V4 | Suscripciones SaaS |
| V5 | Parámetros ERP |
| V6 | Direcciones cliente |
| V8 | Parámetros de tema |
| V9 | Chat |
| V10 | Parámetros de pago / POS PIN login |
| V11 | Password reset tokens |
| V12 | Segmentos |
| V13 | Módulos y configuración de tema |
| V14 | Tabla notifications |
| V15–V16 | Credit accounts y transactions |
| V17 | Company theme config |
| V18 | User theme preferences |
| V19 | ERP parameters editables |
| V20–V40 | POS PIN, seeds de catálogos, landing, logo binario, sesión/JTI, suscripción por dominio |
| V41 | Catálogos SUNAT reales (unidades cat. 03, documentos cat. 06, comprobantes cat. 01) + segmentos |
| V42 | **Tabla `ubigeo`: 25 departamentos / 196 provincias / 1874 distritos del INEI.** Data maestra NACIONAL, sin `tenant_id`. Sirve los tres selects encadenados de dirección y aporta el código de 6 dígitos que SUNAT exige en la guía de remisión. Desnormalizada a propósito (los tres nombres en cada fila) para resolver cada nivel con un `DISTINCT` sin joins. `UbigeoController` está declarado EXENTO en el gate de tenancy con motivo verificado |
| V43 | Desactiva `CATALOGO.UBIGEO_DEPARTAMENTO.*` de `erp_parameters`: tras la V42 era una segunda fuente del mismo maestro y con **códigos incompatibles** (`'AMAZONAS'` vs `'01'`). Se comprobó 0 consumidores en los 6 backends y en app-shop antes de retirarlo; se desactiva en vez de borrarse porque `getCatalog` ya filtra por `is_active` |

### Schema `dbshoprrhh` — última: V12  ·  medido 2026-08-06 (la cabecera decía V5: 7 de desfase)

| Versión | Contenido |
|---|---|
| V1 | Schema inicial RRHH: empleados, departamentos, cargos |
| V2 | Modelo extendido |
| V3 | Pensión AFP y tienda empleado |
| V4 | Evaluaciones 360° |
| V5 | Relación empleado-usuario |

## JWT y Seguridad

- Emite tokens RSA-256 (clave privada solo aquí); otros servicios validan con la clave pública
- Payload JWT: `userId`, `username`, `companyId`
- Sesiones almacenadas en BD (no en memoria)
- Endpoints internos sin JWT: `/api/internal/**` en `permitAll()` de `SecurityConfig`
- `JwtAuthenticationFilter`: capturar `JwtException` (no `Exception`); loguear a `DEBUG`, no `ERROR`

## Entidades clave y gotchas

- **`CompanyEntity`** usa `getName()` / `getRuc()` (campos en inglés) — diferente al resto
- **Multi-tenancy**: todos los datos filtrados por `company_id`; `CompanyEntity` vive aquí
- **Feature flags por tenant**: `COUPONS`, `CREDIT`, `STORE_FOLLOWS`, `SWITCH_ACCOUNT` — verificar con `authService.hasModule('NAME')` en frontend
- **`activeCompanyId`**: el campo correcto en el frontend es `authService.currentUser()?.activeCompanyId`
- **Excepciones**: usar siempre `com.microshop.users.shared.exception.*` en ambos módulos

## Comandos

```bash
cd microshopusers
./mvnw compile -q          # Verificar compilación
./mvnw spring-boot:run     # Arrancar (port 8080)
./mvnw test                # Tests
ls src/main/resources/db/migration/usuarios/ | sort -V | tail -3  # usuarios (V44 el 2026-08-06)  # ojo: `sort -V`, no `sort` (V9 va DESPUES de V43 lexicograficamente)
ls src/main/resources/db/migration/rrhh/ | sort -V | tail -3      # rrhh (V12 el 2026-08-06)
```
