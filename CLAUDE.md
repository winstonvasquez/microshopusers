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

### Schema `dbshopusuarios` — última: V19

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

### Schema `dbshoprrhh` — última: V5

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
ls src/main/resources/db/migration/usuarios/ | sort | tail -3  # Última migración usuarios (V19)
ls src/main/resources/db/migration/rrhh/ | sort | tail -3      # Última migración rrhh (V5)
```
