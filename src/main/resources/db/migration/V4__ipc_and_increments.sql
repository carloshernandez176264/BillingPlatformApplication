-- ============================================================
-- BILLING PLATFORM — V4 — IPC y Incrementos Tarifarios
-- ============================================================

CREATE TABLE ipc_rates (
                           id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                           year           INT          NOT NULL UNIQUE,
                           ipc_percentage NUMERIC(5,2) NOT NULL CHECK (ipc_percentage >= 0),
                           description    VARCHAR(300),
                           source         VARCHAR(100) DEFAULT 'DANE - Colombia',
                           active         BOOLEAN      NOT NULL DEFAULT TRUE,
                           created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                           updated_at     TIMESTAMPTZ,
                           created_by     VARCHAR(100),
                           updated_by     VARCHAR(100)
);

CREATE TABLE tariff_increments (
                                   id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                   client_id      UUID         NOT NULL REFERENCES clients(id),
                                   ipc_rate_id    UUID         NOT NULL REFERENCES ipc_rates(id),
                                   apply_year     INT          NOT NULL,
                                   effective_date DATE         NOT NULL,
                                   ipc_percentage NUMERIC(5,2) NOT NULL,
                                   status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                       CHECK (status IN ('PENDING','APPROVED','REJECTED')),
                                   approved_by    VARCHAR(150),
                                   rejection_reason VARCHAR(500),
                                   observations   VARCHAR(500),
                                   active         BOOLEAN      NOT NULL DEFAULT TRUE,
                                   created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                   updated_at     TIMESTAMPTZ,
                                   created_by     VARCHAR(100),
                                   updated_by     VARCHAR(100)
);

CREATE INDEX idx_ti_client ON tariff_increments(client_id);
CREATE INDEX idx_ti_status ON tariff_increments(status);
CREATE INDEX idx_ti_year   ON tariff_increments(apply_year);

-- IPC histórico Colombia
INSERT INTO ipc_rates (year, ipc_percentage, description, source) VALUES
                                                                      (2020, 1.61,  'IPC Colombia 2020 — afectado por pandemia COVID-19', 'DANE'),
                                                                      (2021, 5.62,  'IPC Colombia 2021 — recuperación económica',          'DANE'),
                                                                      (2022, 13.12, 'IPC Colombia 2022 — máximo histórico reciente',       'DANE'),
                                                                      (2023, 9.28,  'IPC Colombia 2023',                                   'DANE'),
                                                                      (2024, 5.20,  'IPC Colombia 2024',                                   'DANE'),
                                                                      (2025, 5.10,  'IPC Colombia 2025 — proyectado',                      'DANE');