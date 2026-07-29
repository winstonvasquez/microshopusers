-- V38__auditoria_usuario_company.sql
--
-- M05: no habia rastro reconstruible de una escalada de privilegios. `@Audited` cubria 19 entidades,
-- TODAS bajo `rrhh/domain/model`, y cero fuera de rrhh. `UserCommandService.changeRole:134-147` solo
-- hacia `log.info`, que se pierde al rotar los logs. Lo que si existia —`AuditEntity` con @PreUpdate
-- poblando `usuario_modificacion`/`fecha_modificacion`— dice QUIEN toco y CUANDO, pero no el valor
-- ANTERIOR, que es justo lo que hace falta para reconstruir quien se dio a si mismo un rol.
--
-- Se audita con Envers, que es como este proyecto ya audita: hay una unica @RevisionEntity
-- (`RrhhRevisionEntity`) y `hibernate-envers` ya estaba en el pom. Las revisiones de estas tres
-- tablas van a `dbshoprrhh.revinfo`, compartida — de ahi que las FK cruzen de schema (misma base).
--
-- SE DESCARTO la via barata de poblar `policy_audit`: existe, esta mapeada (`PolicyAuditEntity`) y
-- tiene 0 filas porque NADIE la escribe. Su forma casi encajaba, pero es la tabla de auditoria de un
-- motor de politicas muerto: `policy_id` quedaria siempre NULL y el nombre mentiria sobre lo que
-- guarda. Crear otra tabla nueva al lado de una tabla muerta con casi la misma forma es igual de malo.
--
-- EL DDL NO ESTA ESCRITO A MANO. Es la misma precaucion de la V9 de rrhh, pero al reves: alli las
-- tablas ya existian y se volcaron con `pg_dump`; aqui no existen, asi que se genero con el export de
-- esquema de HIBERNATE a fichero
-- (`jakarta.persistence.schema-generation.scripts.action=create` + `scripts.create-target`), que NO
-- toca la base —eso es `database.action`, otra propiedad— y produce exactamente las columnas que
-- Envers va a esperar. Del script resultante se extrajeron solo estas tres tablas y sus FK.
--
-- DOS COLUMNAS EXCLUIDAS A PROPOSITO, con @NotAudited en las entidades:
--   * `usuario.password` y `usuario.pin_hash`. Envers las habria incluido por defecto, y una tabla de
--     auditoria acumulando hashes de credenciales con retencion indefinida es un sitio MAS donde esos
--     hashes viven, con otros permisos de lectura, sin aportar nada al rastro de una escalada.
--   * `company.logo_data`. Envers copiaria el binario del logo en cada cambio de la empresa. Bloat de
--     almacenamiento sin valor de auditoria.
--
-- Tambien se excluyo la coleccion `UserCompanyEntity.roles`, con @NotAudited y no con
-- @Audited(targetAuditMode = NOT_AUDITED): ese modo solo vale para relaciones *to-one*. En una
-- coleccion, Envers exige que el destino este auditado y aborta el arranque con
-- «An audited relation from ... to a not audited entity» — que es exactamente lo que paso al
-- intentarlo del otro modo.


-- Tablas de auditoria ------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS dbshopusuarios.usuario_aud (
    rev integer not null,
    revtype smallint,
    id bigint not null,
    persona_id bigint,
    rol_id bigint,
    username varchar(50),
    email varchar(100),
    primary key (rev, id)
);

CREATE TABLE IF NOT EXISTS dbshopusuarios.company_aud (
    is_active boolean,
    logo_size integer,
    rev integer not null,
    revtype smallint,
    id bigint not null,
    phone varchar(20),
    ruc varchar(20),
    logo_mime varchar(50),
    logo_etag varchar(64),
    domain varchar(100),
    email varchar(100),
    name varchar(100),
    legal_name varchar(200),
    address varchar(300),
    logo_url varchar(500),
    primary key (rev, id)
);

CREATE TABLE IF NOT EXISTS dbshopusuarios.user_company_aud (
    is_active boolean,
    rev integer not null,
    revtype smallint,
    company_id bigint,
    id bigint not null,
    usuario_id bigint,
    primary key (rev, id)
);


-- FK del numero de revision hacia la tabla de revisiones compartida ---------------------

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshopusuarios' AND r.relname = 'company_aud'
                      AND c.conname = 'FK4bojjw2sh9ku0m2giux40mu3h') THEN
        ALTER TABLE dbshopusuarios.company_aud
            ADD CONSTRAINT FK4bojjw2sh9ku0m2giux40mu3h FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshopusuarios' AND r.relname = 'user_company_aud'
                      AND c.conname = 'FKpw3uso2ey7gw1cmgytbntf97f') THEN
        ALTER TABLE dbshopusuarios.user_company_aud
            ADD CONSTRAINT FKpw3uso2ey7gw1cmgytbntf97f FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint c
                     JOIN pg_class r ON r.oid = c.conrelid
                     JOIN pg_namespace n ON n.oid = r.relnamespace
                    WHERE n.nspname = 'dbshopusuarios' AND r.relname = 'usuario_aud'
                      AND c.conname = 'FK74gdm3bhlqa3diq16ouihfq6e') THEN
        ALTER TABLE dbshopusuarios.usuario_aud
            ADD CONSTRAINT FK74gdm3bhlqa3diq16ouihfq6e FOREIGN KEY (rev) REFERENCES dbshoprrhh.revinfo(rev);
    END IF;
END $$;
