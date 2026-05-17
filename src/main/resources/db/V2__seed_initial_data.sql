-- ============================================================
-- BILLING PLATFORM — V2 — Datos Iniciales
-- ============================================================

-- Monedas
INSERT INTO currencies (code, name, symbol, decimal_places) VALUES
                                                                ('COP', 'Peso Colombiano',   '$',  0),
                                                                ('USD', 'Dólar Americano',   '$',  2),
                                                                ('EUR', 'Euro',              '€',  2);

-- Permisos
INSERT INTO permissions (name, description, module) VALUES
                                                        ('CREATE_CLIENT',          'Crear nuevos clientes',              'clientes'),
                                                        ('READ_CLIENT',            'Ver información de clientes',        'clientes'),
                                                        ('UPDATE_CLIENT',          'Editar información de clientes',     'clientes'),
                                                        ('DELETE_CLIENT',          'Desactivar clientes',                'clientes'),
                                                        ('CREATE_RATE',            'Crear tarifas de facturación',       'tarifas'),
                                                        ('READ_RATE',              'Ver tarifas de facturación',         'tarifas'),
                                                        ('UPDATE_RATE',            'Editar tarifas de facturación',      'tarifas'),
                                                        ('DELETE_RATE',            'Desactivar tarifas',                 'tarifas'),
                                                        ('CREATE_WORK_LOG',        'Registrar horas de trabajo',         'registros'),
                                                        ('READ_WORK_LOG',          'Ver registros de horas',             'registros'),
                                                        ('UPDATE_WORK_LOG',        'Editar registros de horas',          'registros'),
                                                        ('CREATE_BILLING_NOVELTY', 'Crear novedades de nómina',          'novedades'),
                                                        ('READ_BILLING_NOVELTY',   'Ver novedades de nómina',            'novedades'),
                                                        ('UPDATE_BILLING_NOVELTY', 'Editar novedades de nómina',         'novedades'),
                                                        ('GENERATE_PRE_INVOICE',   'Generar pre-facturas',               'prefacturas'),
                                                        ('APPROVE_PRE_INVOICE',    'Aprobar pre-facturas',               'prefacturas'),
                                                        ('EXPORT_PRE_INVOICE',     'Exportar pre-facturas',              'prefacturas'),
                                                        ('VIEW_REPORTS',           'Ver reportes financieros',           'reportes'),
                                                        ('MANAGE_USERS',           'Gestionar usuarios de la plataforma','usuarios');

-- Roles
INSERT INTO roles (id, name, description) VALUES
                                              (gen_random_uuid(), 'ADMIN',         'Administrador total del sistema'),
                                              (gen_random_uuid(), 'FINANCE',       'Gestor de facturación y finanzas'),
                                              (gen_random_uuid(), 'MANAGER',       'Gerente de operaciones'),
                                              (gen_random_uuid(), 'CLIENT_VIEWER', 'Acceso de solo lectura a clientes'),
                                              (gen_random_uuid(), 'AUDITOR',       'Rol de auditoría y cumplimiento');

-- ADMIN obtiene todos los permisos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN';

-- Permisos FINANCE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'FINANCE'
  AND p.name IN (
                 'READ_CLIENT','CREATE_WORK_LOG','READ_WORK_LOG','UPDATE_WORK_LOG',
                 'CREATE_BILLING_NOVELTY','READ_BILLING_NOVELTY','UPDATE_BILLING_NOVELTY',
                 'GENERATE_PRE_INVOICE','APPROVE_PRE_INVOICE','EXPORT_PRE_INVOICE',
                 'VIEW_REPORTS','READ_RATE'
    );

-- Permisos MANAGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'MANAGER'
  AND p.name IN (
                 'READ_CLIENT','UPDATE_CLIENT','READ_RATE','CREATE_RATE','UPDATE_RATE',
                 'CREATE_WORK_LOG','READ_WORK_LOG','UPDATE_WORK_LOG',
                 'READ_BILLING_NOVELTY','VIEW_REPORTS','GENERATE_PRE_INVOICE'
    );

-- Permisos CLIENT_VIEWER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'CLIENT_VIEWER'
  AND p.name IN ('READ_CLIENT','READ_RATE','READ_WORK_LOG','VIEW_REPORTS');

-- Permisos AUDITOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'AUDITOR'
  AND p.name IN (
                 'READ_CLIENT','READ_RATE','READ_WORK_LOG','READ_BILLING_NOVELTY','VIEW_REPORTS'
    );

-- Perfiles de desarrollador
INSERT INTO developer_profiles (name, level, description) VALUES
                                                              ('Desarrollador Junior',       'JUNIOR',    'Desarrollador principiante, hasta 2 años de experiencia'),
                                                              ('Desarrollador Semi Senior',  'MID',       'Desarrollador con 2 a 4 años de experiencia'),
                                                              ('Desarrollador Senior',       'SENIOR',    'Desarrollador experimentado con más de 4 años'),
                                                              ('Líder Técnico',              'LEAD',      'Liderazgo técnico y arquitectura de soluciones'),
                                                              ('Analista QA',                'MID',       'Analista de calidad manual'),
                                                              ('QA Automatización',          'MID',       'Ingeniero de automatización de pruebas'),
                                                              ('Ingeniero DevOps',           'SENIOR',    'Especialista en infraestructura y CI/CD'),
                                                              ('Arquitecto de Software',     'PRINCIPAL', 'Arquitecto de software empresarial'),
                                                              ('Scrum Master',               'MID',       'Facilitador de procesos ágiles'),
                                                              ('Product Owner',              'MID',       'Dueño del backlog y requisitos del producto'),
                                                              ('Ingeniero de Datos',         'SENIOR',    'Pipelines de datos e infraestructura'),
                                                              ('Desarrollador Backend',      'MID',       'Especialista en desarrollo del lado del servidor'),
                                                              ('Desarrollador Frontend',     'MID',       'Especialista en desarrollo del lado del cliente'),
                                                              ('Desarrollador Fullstack',    'MID',       'Desarrollo de aplicaciones completas');

-- Usuario administrador
-- Contraseña: Admin@123456 (BCrypt strength 12 — debe cambiarla en el primer ingreso)
INSERT INTO users (id, email, password_hash, full_name, status, must_change_password)
VALUES (
           gen_random_uuid(),
           'admin@billing.platform',
           '$2a$12$Ej1/Wd876nvl0164OFmVyu2IuVtL7A7ZmjDw3hiFA4kjfo/WlqHd6',
           'Administrador del Sistema',
           'ACTIVE',
           TRUE
       );

-- Asignar rol ADMIN al usuario administrador
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@billing.platform' AND r.name = 'ADMIN';