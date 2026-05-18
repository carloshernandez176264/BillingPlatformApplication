-- ============================================================
-- BILLING PLATFORM — V6 — Salario Base Desarrollador
-- ============================================================

ALTER TABLE public.developers
    ADD COLUMN IF NOT EXISTS base_salary NUMERIC(19,4);