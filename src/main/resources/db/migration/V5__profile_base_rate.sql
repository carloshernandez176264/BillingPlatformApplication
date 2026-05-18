-- ============================================================
-- BILLING PLATFORM — V5 — Tarifa Base por Perfil
-- ============================================================

ALTER TABLE public.developer_profiles
    ADD COLUMN base_monthly_rate NUMERIC(19,4);

-- Actualizar perfiles existentes con tarifas base de referencia
-- (ajusta estos valores según tu lista de precios real)
UPDATE public.developer_profiles SET base_monthly_rate = 8000000   WHERE level = 'JUNIOR';
UPDATE public.developer_profiles SET base_monthly_rate = 12000000  WHERE level = 'MID';
UPDATE public.developer_profiles SET base_monthly_rate = 18000000  WHERE level = 'SENIOR';
UPDATE public.developer_profiles SET base_monthly_rate = 22000000  WHERE level = 'LEAD';
UPDATE public.developer_profiles SET base_monthly_rate = 25000000  WHERE level = 'PRINCIPAL';