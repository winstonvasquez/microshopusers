-- V4: Soporte evaluación 360 y ciclos de evaluación

ALTER TABLE performance_evaluation ADD COLUMN IF NOT EXISTS tipo_relacion_evaluador VARCHAR(30) DEFAULT 'SUPERVISOR';

ALTER TABLE performance_evaluation ADD COLUMN IF NOT EXISTS ciclo_evaluacion_id BIGINT;

CREATE TABLE IF NOT EXISTS evaluation_cycle (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    periodo_inicio DATE NOT NULL,
    periodo_fin DATE NOT NULL,
    estado VARCHAR(20) DEFAULT 'ABIERTO',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evaluation_cycle_tenant ON evaluation_cycle(tenant_id);
