-- =============================================
-- BILLING PLATFORM - V1 - Initial Schema
-- =============================================

-- EXTENSION para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- BILLING PLATFORM — V1 — Initial Schema
-- ============================================================


-- ---- currencies ----
CREATE TABLE currencies (
                            id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                            code           VARCHAR(10)  NOT NULL UNIQUE,
                            name           VARCHAR(100) NOT NULL,
                            symbol         VARCHAR(10)  NOT NULL,
                            decimal_places INT          NOT NULL DEFAULT 2,
                            active         BOOLEAN      NOT NULL DEFAULT TRUE,
                            created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                            updated_at     TIMESTAMPTZ,
                            created_by     VARCHAR(100),
                            updated_by     VARCHAR(100)
);

-- ---- exchange_rates ----
CREATE TABLE exchange_rates (
                                id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
                                from_currency_id UUID           NOT NULL REFERENCES currencies(id),
                                to_currency_id   UUID           NOT NULL REFERENCES currencies(id),
                                rate             NUMERIC(19,8)  NOT NULL CHECK (rate > 0),
                                valid_from       DATE           NOT NULL,
                                valid_until      DATE,
                                active           BOOLEAN        NOT NULL DEFAULT TRUE,
                                created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                                updated_at       TIMESTAMPTZ,
                                created_by       VARCHAR(100),
                                updated_by       VARCHAR(100),
                                CONSTRAINT uq_exchange_rate_period
                                    UNIQUE (from_currency_id, to_currency_id, valid_from),
                                CONSTRAINT chk_exchange_rate_dates
                                    CHECK (valid_until IS NULL OR valid_until >= valid_from),
                                CONSTRAINT chk_different_currencies
                                    CHECK (from_currency_id <> to_currency_id)
);

-- ---- permissions ----
CREATE TABLE permissions (
                             id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                             name        VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(300),
                             module      VARCHAR(100),
                             active      BOOLEAN      NOT NULL DEFAULT TRUE,
                             created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                             updated_at  TIMESTAMPTZ,
                             created_by  VARCHAR(100),
                             updated_by  VARCHAR(100)
);

-- ---- roles ----
CREATE TABLE roles (
                       id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       name        VARCHAR(50)  NOT NULL UNIQUE,
                       description VARCHAR(200),
                       active      BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMPTZ,
                       created_by  VARCHAR(100),
                       updated_by  VARCHAR(100)
);

-- ---- role_permissions ----
CREATE TABLE role_permissions (
                                  role_id       UUID NOT NULL REFERENCES roles(id)       ON DELETE CASCADE,
                                  permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                  PRIMARY KEY (role_id, permission_id)
);

-- ---- users ----
CREATE TABLE users (
                       id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       email                 VARCHAR(150) NOT NULL UNIQUE,
                       password_hash         VARCHAR(255) NOT NULL,
                       full_name             VARCHAR(200) NOT NULL,
                       status                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                           CHECK (status IN ('ACTIVE','INACTIVE','BLOCKED','SUSPENDED')),
                       locked                BOOLEAN      NOT NULL DEFAULT FALSE,
                       failed_login_attempts INT          NOT NULL DEFAULT 0,
                       last_login_at         TIMESTAMPTZ,
                       password_changed_at   TIMESTAMPTZ,
                       must_change_password  BOOLEAN      NOT NULL DEFAULT FALSE,
                       active                BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at            TIMESTAMPTZ,
                       created_by            VARCHAR(100),
                       updated_by            VARCHAR(100)
);
CREATE INDEX idx_users_email  ON users(email);
CREATE INDEX idx_users_status ON users(status);

-- ---- user_roles ----
CREATE TABLE user_roles (
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

-- ---- refresh_tokens ----
CREATE TABLE refresh_tokens (
                                id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token_hash  VARCHAR(255) NOT NULL UNIQUE,
                                expires_at  TIMESTAMPTZ  NOT NULL,
                                revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
                                revoked_at  TIMESTAMPTZ,
                                created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                user_agent  VARCHAR(300),
                                ip_address  VARCHAR(45)
);
CREATE INDEX idx_rt_user  ON refresh_tokens(user_id);
CREATE INDEX idx_rt_token ON refresh_tokens(token_hash);

-- ---- clients ----
CREATE TABLE clients (
                         id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         tax_id              VARCHAR(30)  NOT NULL UNIQUE,
                         company_name        VARCHAR(200) NOT NULL,
                         trade_name          VARCHAR(200),
                         country             VARCHAR(60)  NOT NULL,
                         city                VARCHAR(100),
                         address             VARCHAR(300),
                         billing_email       VARCHAR(150) NOT NULL,
                         contact_name        VARCHAR(200),
                         contact_phone       VARCHAR(30),
                         primary_currency_id UUID         NOT NULL REFERENCES currencies(id),
                         tax_regime          VARCHAR(100),
                         notes               TEXT,
                         status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                             CHECK (status IN ('ACTIVE','INACTIVE','SUSPENDED')),
                         active              BOOLEAN      NOT NULL DEFAULT TRUE,
                         created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                         updated_at          TIMESTAMPTZ,
                         created_by          VARCHAR(100),
                         updated_by          VARCHAR(100)
);
CREATE INDEX idx_clients_tax_id ON clients(tax_id);
CREATE INDEX idx_clients_status ON clients(status);

-- ---- developer_profiles ----
CREATE TABLE developer_profiles (
                                    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                    name        VARCHAR(100) NOT NULL UNIQUE,
                                    level       VARCHAR(30),
                                    description TEXT,
                                    base_skills TEXT,
                                    active      BOOLEAN      NOT NULL DEFAULT TRUE,
                                    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                    updated_at  TIMESTAMPTZ,
                                    created_by  VARCHAR(100),
                                    updated_by  VARCHAR(100)
);

-- ---- developers ----
CREATE TABLE developers (
                            id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                            document_id     VARCHAR(30)  NOT NULL UNIQUE,
                            document_type   VARCHAR(20)  NOT NULL,
                            full_name       VARCHAR(200) NOT NULL,
                            email           VARCHAR(150),
                            profile_id      UUID         NOT NULL REFERENCES developer_profiles(id),
                            hire_date       DATE         NOT NULL,
                            assignment_mode VARCHAR(50),
                            status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE','INACTIVE','ON_LEAVE')),
                            active          BOOLEAN      NOT NULL DEFAULT TRUE,
                            created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                            updated_at      TIMESTAMPTZ,
                            created_by      VARCHAR(100),
                            updated_by      VARCHAR(100)
);
CREATE INDEX idx_developers_profile ON developers(profile_id);
CREATE INDEX idx_developers_status  ON developers(status);

-- ---- rates ----
CREATE TABLE rates (
                       id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                       client_id             UUID          REFERENCES clients(id),
                       developer_profile_id  UUID          NOT NULL REFERENCES developer_profiles(id),
                       currency_id           UUID          NOT NULL REFERENCES currencies(id),
                       rate_type             VARCHAR(20)   NOT NULL CHECK (rate_type IN ('MONTHLY','DAILY','HOURLY')),
                       monthly_rate          NUMERIC(19,4),
                       daily_rate            NUMERIC(19,4),
                       hourly_rate           NUMERIC(19,4),
                       valid_from            DATE          NOT NULL,
                       valid_until           DATE,
                       includes_tax          BOOLEAN       NOT NULL DEFAULT FALSE,
                       tax_percentage        NUMERIC(5,2),
                       discount_percentage   NUMERIC(5,2)  NOT NULL DEFAULT 0,
                       working_hours_per_day NUMERIC(4,2)  NOT NULL DEFAULT 8.00,
                       status                VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
                           CHECK (status IN ('ACTIVE','INACTIVE','EXPIRED')),
                       commercial_notes      TEXT,
                       active                BOOLEAN       NOT NULL DEFAULT TRUE,
                       created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                       updated_at            TIMESTAMPTZ,
                       created_by            VARCHAR(100),
                       updated_by            VARCHAR(100),
                       CONSTRAINT chk_rate_dates CHECK (valid_until IS NULL OR valid_until >= valid_from),
                       CONSTRAINT chk_rate_has_value
                           CHECK (monthly_rate IS NOT NULL OR daily_rate IS NOT NULL OR hourly_rate IS NOT NULL)
);
CREATE INDEX idx_rates_client_profile ON rates(client_id, developer_profile_id);
CREATE INDEX idx_rates_status         ON rates(status);

-- ---- work_logs ----
CREATE TABLE work_logs (
                           id                     UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                           client_id              UUID          NOT NULL REFERENCES clients(id),
                           developer_id           UUID          NOT NULL REFERENCES developers(id),
                           developer_profile_id   UUID          NOT NULL REFERENCES developer_profiles(id),
                           applied_rate_id        UUID          REFERENCES rates(id),
                           billing_year           INT           NOT NULL,
                           billing_month          INT           NOT NULL CHECK (billing_month BETWEEN 1 AND 12),
                           expected_working_days  INT           NOT NULL CHECK (expected_working_days > 0),
                           expected_working_hours NUMERIC(6,2)  NOT NULL CHECK (expected_working_hours > 0),
                           actual_worked_hours    NUMERIC(6,2)  NOT NULL CHECK (actual_worked_hours >= 0),
                           billable_amount        NUMERIC(19,4),
                           observations           TEXT,
                           status                 VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
                               CHECK (status IN ('DRAFT','CONFIRMED','BILLED')),
                           active                 BOOLEAN       NOT NULL DEFAULT TRUE,
                           created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                           updated_at             TIMESTAMPTZ,
                           created_by             VARCHAR(100),
                           updated_by             VARCHAR(100),
                           CONSTRAINT uq_work_log_period
                               UNIQUE (client_id, developer_id, developer_profile_id, billing_year, billing_month)
);
CREATE INDEX idx_wl_client_period ON work_logs(client_id, billing_year, billing_month);
CREATE INDEX idx_wl_developer     ON work_logs(developer_id);

-- ---- billing_novelties ----
CREATE TABLE billing_novelties (
                                   id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                                   work_log_id           UUID          NOT NULL REFERENCES work_logs(id),
                                   developer_id          UUID          NOT NULL REFERENCES developers(id),
                                   client_id             UUID          NOT NULL REFERENCES clients(id),
                                   novelty_type          VARCHAR(40)   NOT NULL,
                                   unit_type             VARCHAR(10)   NOT NULL CHECK (unit_type IN ('DAYS','HOURS','BOTH')),
                                   affected_days         NUMERIC(4,1)  NOT NULL DEFAULT 0 CHECK (affected_days >= 0),
                                   affected_hours        NUMERIC(5,2)  NOT NULL DEFAULT 0 CHECK (affected_hours >= 0),
                                   start_date            DATE,
                                   end_date              DATE,
                                   calculated_discount   NUMERIC(19,4),
                                   manual_discount_value NUMERIC(19,4),
                                   billing_year          INT           NOT NULL,
                                   billing_month         INT           NOT NULL CHECK (billing_month BETWEEN 1 AND 12),
                                   observations          TEXT,
                                   support_document_id   UUID,
                                   approval_status       VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                                       CHECK (approval_status IN ('PENDING','APPROVED','REJECTED')),
                                   approved_by           VARCHAR(150),
                                   rejection_reason      TEXT,
                                   active                BOOLEAN       NOT NULL DEFAULT TRUE,
                                   created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                                   updated_at            TIMESTAMPTZ,
                                   created_by            VARCHAR(100),
                                   updated_by            VARCHAR(100),
                                   CONSTRAINT chk_novelty_dates CHECK (end_date IS NULL OR end_date >= start_date)
);
CREATE INDEX idx_novelty_worklog     ON billing_novelties(work_log_id);
CREATE INDEX idx_novelty_dev_period  ON billing_novelties(developer_id, billing_year, billing_month);

-- ---- pre_invoices ----
CREATE TABLE pre_invoices (
                              id                      UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                              invoice_number          VARCHAR(30)   NOT NULL UNIQUE,
                              client_id               UUID          NOT NULL REFERENCES clients(id),
                              currency_id             UUID          NOT NULL REFERENCES currencies(id),
                              billing_year            INT           NOT NULL,
                              billing_month           INT           NOT NULL CHECK (billing_month BETWEEN 1 AND 12),
                              period_description      VARCHAR(100),
                              subtotal                NUMERIC(19,4) NOT NULL DEFAULT 0,
                              total_novelty_discounts NUMERIC(19,4) NOT NULL DEFAULT 0,
                              total_other_discounts   NUMERIC(19,4) NOT NULL DEFAULT 0,
                              taxable_amount          NUMERIC(19,4) NOT NULL DEFAULT 0,
                              tax_amount              NUMERIC(19,4) NOT NULL DEFAULT 0,
                              total_amount            NUMERIC(19,4) NOT NULL DEFAULT 0,
                              generation_date         DATE          NOT NULL,
                              due_date                DATE,
                              status                  VARCHAR(30)   NOT NULL DEFAULT 'DRAFT'
                                  CHECK (status IN ('DRAFT','GENERATED','SENT_TO_CLIENT',
                                                    'APPROVED','REJECTED','CANCELLED','INVOICED')),
                              observations            TEXT,
                              rejection_reason        TEXT,
                              version                 INT           NOT NULL DEFAULT 1,
                              active                  BOOLEAN       NOT NULL DEFAULT TRUE,
                              created_at              TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                              updated_at              TIMESTAMPTZ,
                              created_by              VARCHAR(100),
                              updated_by              VARCHAR(100)
);
CREATE INDEX idx_pi_client ON pre_invoices(client_id);
CREATE INDEX idx_pi_period ON pre_invoices(billing_year, billing_month);
CREATE INDEX idx_pi_status ON pre_invoices(status);

-- ---- pre_invoice_items ----
CREATE TABLE pre_invoice_items (
                                   id                   UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                                   pre_invoice_id       UUID          NOT NULL REFERENCES pre_invoices(id) ON DELETE CASCADE,
                                   work_log_id          UUID          NOT NULL REFERENCES work_logs(id),
                                   developer_id         UUID          NOT NULL REFERENCES developers(id),
                                   developer_profile_id UUID          NOT NULL REFERENCES developer_profiles(id),
                                   rate_id              UUID          REFERENCES rates(id),
                                   rate_type            VARCHAR(20),
                                   rate_value           NUMERIC(19,4),
                                   billed_hours         NUMERIC(6,2),
                                   billed_days          NUMERIC(4,1),
                                   gross_amount         NUMERIC(19,4) NOT NULL DEFAULT 0,
                                   novelty_discount     NUMERIC(19,4) NOT NULL DEFAULT 0,
                                   other_discount       NUMERIC(19,4) NOT NULL DEFAULT 0,
                                   net_amount           NUMERIC(19,4) NOT NULL DEFAULT 0,
                                   line_description     VARCHAR(500),
                                   sort_order           INT           NOT NULL DEFAULT 0,
                                   created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_pi_items_invoice ON pre_invoice_items(pre_invoice_id);

-- ---- audit_logs ----
CREATE TABLE audit_logs (
                            id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                            entity_type     VARCHAR(100) NOT NULL,
                            entity_id       VARCHAR(100) NOT NULL,
                            action          VARCHAR(50)  NOT NULL,
                            old_values      JSONB,
                            new_values      JSONB,
                            performed_by    VARCHAR(150) NOT NULL,
                            performed_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                            ip_address      VARCHAR(45),
                            user_agent      VARCHAR(300),
                            request_id      VARCHAR(100),
                            additional_info JSONB
);
CREATE INDEX idx_audit_entity    ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_performer ON audit_logs(performed_by);
CREATE INDEX idx_audit_date      ON audit_logs(performed_at);
CREATE INDEX idx_audit_action    ON audit_logs(action);

-- ---- file_attachments ----
CREATE TABLE file_attachments (
                                  id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                  original_name VARCHAR(255) NOT NULL,
                                  stored_name   VARCHAR(255) NOT NULL UNIQUE,
                                  content_type  VARCHAR(100) NOT NULL,
                                  file_size     BIGINT       NOT NULL,
                                  storage_path  VARCHAR(500) NOT NULL,
                                  entity_type   VARCHAR(100),
                                  entity_id     UUID,
                                  uploaded_by   VARCHAR(150) NOT NULL,
                                  active        BOOLEAN      NOT NULL DEFAULT TRUE,
                                  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);