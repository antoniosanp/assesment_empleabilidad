-- =============================================================================
-- Migration V3: Seed Data Insertion (Matching seed.json corpus)
-- Password for all test users is '123456' hashed with BCrypt.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Insert Initial Users
-- -----------------------------------------------------------------------------
INSERT INTO rw_users (rw_id, rw_email, rw_password_hash, rw_full_name, rw_job_title, rw_role, rw_is_active)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin@riwi.io', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Admin Sistema', 'System Administrator & Security Lead', 'ADMIN', true),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'juan.perez@riwi.io', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Juan Perez', 'Senior Frontend Developer', 'MEMBER', true),
    ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'maria.gomez@riwi.io', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Maria Gomez', 'Lead Backend Engineer', 'MEMBER', true),
    ('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'pedro.soporte@riwi.io', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Pedro Soporte', 'IT Support Specialist', 'MEMBER', true)
ON CONFLICT (rw_id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 2. Insert Initial Channels
-- -----------------------------------------------------------------------------
INSERT INTO rw_channels (rw_id, rw_name, rw_type, rw_created_by)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'General', 'PUBLIC', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
    ('22222222-2222-2222-2222-222222222222', 'Desarrollo Backend', 'PRIVATE', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33'),
    ('33333333-3333-3333-3333-333333333333', 'Directo Juan-Maria', 'DIRECT', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22')
ON CONFLICT (rw_id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 3. Insert Channel Memberships
-- -----------------------------------------------------------------------------
INSERT INTO rw_channel_members (rw_channel_id, rw_user_id, rw_member_role)
VALUES 
    -- General Channel Members
    ('11111111-1111-1111-1111-111111111111', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'OWNER'),
    ('11111111-1111-1111-1111-111111111111', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'MEMBER'),
    ('11111111-1111-1111-1111-111111111111', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'MEMBER'),
    ('11111111-1111-1111-1111-111111111111', 'd3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'MEMBER'),
    
    -- Backend Private Channel Members (Admin & Maria only)
    ('22222222-2222-2222-2222-222222222222', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'OWNER'),
    ('22222222-2222-2222-2222-222222222222', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'ADMIN'),

    -- Direct Chat Members (Juan & Maria only)
    ('33333333-3333-3333-3333-333333333333', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'MEMBER'),
    ('33333333-3333-3333-3333-333333333333', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'MEMBER')
ON CONFLICT (rw_channel_id, rw_user_id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 4. Insert Initial Sample Messages
-- -----------------------------------------------------------------------------
INSERT INTO rw_messages (rw_id, rw_channel_id, rw_sender_id, rw_content, rw_status, rw_is_edited, rw_is_deleted)
VALUES 
    (1, '11111111-1111-1111-1111-111111111111', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Bienvenidos a la nueva plataforma de mensajería interna de Riwi Co. S.A.S.', 'SENT', false, false),
    (2, '11111111-1111-1111-1111-111111111111', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '¡Hola equipo! Excelente iniciativa para mejorar la comunicación interna.', 'SENT', false, false),
    (3, '22222222-2222-2222-2222-222222222222', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Recordatorio privado: La migración a PostgreSQL 15 con Row Level Security y pgvector fue completada con éxito.', 'SENT', false, false),
    (4, '33333333-3333-3333-3333-333333333333', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Hola Maria, te envié los diseños actualizados del frontend por este chat directo.', 'SENT', false, false)
ON CONFLICT (rw_id) DO NOTHING;

-- Synchronize sequence after manual insertion of primary key IDs
SELECT setval('rw_messages_rw_id_seq', (SELECT MAX(rw_id) FROM rw_messages));
