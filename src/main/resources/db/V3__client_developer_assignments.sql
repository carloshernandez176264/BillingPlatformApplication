-- ============================================================
-- BILLING PLATFORM — V3 — Asignaciones Cliente-Desarrollador
-- ============================================================

CREATE TABLE client_developer_assignments (
                                              id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                              client_id    UUID        NOT NULL REFERENCES clients(id),
                                              developer_id UUID        NOT NULL REFERENCES developers(id),
                                              active       BOOLEAN     NOT NULL DEFAULT TRUE,
                                              notes        VARCHAR(500),
                                              created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                              updated_at   TIMESTAMPTZ,
                                              created_by   VARCHAR(100),
                                              updated_by   VARCHAR(100),
                                              CONSTRAINT uq_client_developer UNIQUE (client_id, developer_id)
);

CREATE INDEX idx_cda_client    ON client_developer_assignments(client_id);
CREATE INDEX idx_cda_developer ON client_developer_assignments(developer_id);
CREATE INDEX idx_cda_active    ON client_developer_assignments(active);