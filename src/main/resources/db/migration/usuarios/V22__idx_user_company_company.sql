-- Hardening 2026-05-28: índice sobre company_id en user_company.
-- Contexto: uk_user_company es UNIQUE(usuario_id, company_id) — company_id NO es columna
-- líder del índice compuesto, por lo que findByCompanyId (UserCompanyRepository) y el nuevo
-- findByPinHashIsNotNullAndCompanyId (pinLogin acotado por empresa) hacen full scan de
-- user_company. Se agrega índice dedicado por company_id.
CREATE INDEX IF NOT EXISTS idx_user_company_company ON user_company(company_id);
