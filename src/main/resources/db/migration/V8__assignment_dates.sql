ALTER TABLE public.client_developer_assignments
    ADD COLUMN IF NOT EXISTS start_date DATE,
    ADD COLUMN IF NOT EXISTS end_date   DATE;

COMMENT ON COLUMN public.client_developer_assignments.start_date
    IS 'Fecha de inicio del desarrollador en este cliente';
COMMENT ON COLUMN public.client_developer_assignments.end_date
    IS 'Fecha de fin del desarrollador en este cliente (null = activo)';