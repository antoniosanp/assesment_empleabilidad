-- =============================================================================
-- Migration V1: Initial Schema for Riwi Internal Messaging System
-- Database Engine: PostgreSQL 15+
-- All tables and columns must use English names and start with 'rw_' prefix.
-- All timestamps use timestamptz in UTC.
-- =============================================================================

-- Enable pgvector extension for AI Copilot RAG embeddings
CREATE EXTENSION IF NOT EXISTS vector;

-- -----------------------------------------------------------------------------
-- Table: rw_users
-- Stores user accounts, authentication hashes, and user roles/job titles.
-- -----------------------------------------------------------------------------
CREATE TABLE rw_users (
    rw_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rw_email VARCHAR(255) NOT NULL UNIQUE,
    rw_password_hash VARCHAR(255) NOT NULL,
    rw_full_name VARCHAR(150) NOT NULL,
    rw_job_title VARCHAR(100) NOT NULL,
    rw_role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    rw_is_active BOOLEAN NOT NULL DEFAULT true,
    rw_created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    rw_updated_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    CONSTRAINT rw_chk_user_email_format CHECK (rw_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT rw_chk_user_role CHECK (rw_role IN ('ADMIN', 'MEMBER'))
);

-- -----------------------------------------------------------------------------
-- Table: rw_channels
-- Stores communication channels (Public, Private, or Direct Messages).
-- -----------------------------------------------------------------------------
CREATE TABLE rw_channels (
    rw_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rw_name VARCHAR(100) NULL,
    rw_type VARCHAR(20) NOT NULL,
    rw_created_by UUID NULL,
    rw_created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    rw_updated_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    -- Justification ON DELETE SET NULL: If a channel creator user is deleted, 
    -- the channel remains active to preserve team communication history.
    CONSTRAINT rw_fk_channels_created_by FOREIGN KEY (rw_created_by) 
        REFERENCES rw_users(rw_id) ON DELETE SET NULL,
    CONSTRAINT rw_chk_channel_type CHECK (rw_type IN ('PUBLIC', 'PRIVATE', 'DIRECT'))
);

-- -----------------------------------------------------------------------------
-- Table: rw_channel_members
-- Junction table representing user memberships and permissions per channel.
-- -----------------------------------------------------------------------------
CREATE TABLE rw_channel_members (
    rw_id BIGSERIAL PRIMARY KEY,
    rw_channel_id UUID NOT NULL,
    rw_user_id UUID NOT NULL,
    rw_member_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    rw_joined_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    rw_last_read_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    -- Justification ON DELETE CASCADE: When a channel is deleted, all member links are removed.
    CONSTRAINT rw_fk_members_channel FOREIGN KEY (rw_channel_id) 
        REFERENCES rw_channels(rw_id) ON DELETE CASCADE,
    -- Justification ON DELETE CASCADE: When a user account is deleted, their membership links are cleaned up.
    CONSTRAINT rw_fk_members_user FOREIGN KEY (rw_user_id) 
        REFERENCES rw_users(rw_id) ON DELETE CASCADE,
    CONSTRAINT rw_uq_channel_user UNIQUE (rw_channel_id, rw_user_id),
    CONSTRAINT rw_chk_member_role CHECK (rw_member_role IN ('OWNER', 'ADMIN', 'MEMBER'))
);

-- -----------------------------------------------------------------------------
-- Table: rw_messages
-- Stores chat messages, edit history, vector embeddings, and soft delete state.
-- -----------------------------------------------------------------------------
CREATE TABLE rw_messages (
    rw_id BIGSERIAL PRIMARY KEY,
    rw_channel_id UUID NOT NULL,
    rw_sender_id UUID NOT NULL,
    rw_content TEXT NOT NULL,
    rw_original_content TEXT NULL,
    rw_status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    rw_is_edited BOOLEAN NOT NULL DEFAULT false,
    rw_is_deleted BOOLEAN NOT NULL DEFAULT false,
    rw_deleted_at TIMESTAMPTZ NULL,
    rw_embedding vector(1536) NULL,
    rw_created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    rw_updated_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    -- Justification ON DELETE RESTRICT: Prevents deletion of channels containing active messages.
    CONSTRAINT rw_fk_messages_channel FOREIGN KEY (rw_channel_id) 
        REFERENCES rw_channels(rw_id) ON DELETE RESTRICT,
    -- Justification ON DELETE RESTRICT: Prevents hard-deletion of users who sent messages to preserve chat logs.
    CONSTRAINT rw_fk_messages_sender FOREIGN KEY (rw_sender_id) 
        REFERENCES rw_users(rw_id) ON DELETE RESTRICT,
    CONSTRAINT rw_chk_message_status CHECK (rw_status IN ('PENDING', 'SENT', 'FAILED'))
);

-- -----------------------------------------------------------------------------
-- Table: rw_copilot_usage_logs
-- Tracks AI Copilot token consumption and interaction audit per user.
-- -----------------------------------------------------------------------------
CREATE TABLE rw_copilot_usage_logs (
    rw_id BIGSERIAL PRIMARY KEY,
    rw_user_id UUID NOT NULL,
    rw_prompt TEXT NOT NULL,
    rw_response TEXT NOT NULL,
    rw_tokens_used INT NOT NULL DEFAULT 0,
    rw_created_at TIMESTAMPTZ NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    -- Justification ON DELETE CASCADE: Deleting a user cleans up their AI consumption audit log.
    CONSTRAINT rw_fk_copilot_logs_user FOREIGN KEY (rw_user_id) 
        REFERENCES rw_users(rw_id) ON DELETE CASCADE,
    CONSTRAINT rw_chk_copilot_tokens_positive CHECK (rw_tokens_used >= 0)
);

-- =============================================================================
-- INDEXES
-- =============================================================================

-- Partial Unique Index (Requirement: "al menos un índice único parcial")
-- Guarantees that a user cannot have duplicate PENDING messages sent at the exact same timestamp in a channel.
CREATE UNIQUE INDEX rw_idx_unique_pending_msg 
    ON rw_messages (rw_channel_id, rw_sender_id, rw_created_at) 
    WHERE rw_status = 'PENDING';

-- Keyset Pagination Index (Requirement: "historial de mensajes de un canal con paginación por keyset")
CREATE INDEX rw_idx_messages_channel_keyset 
    ON rw_messages (rw_channel_id, rw_created_at DESC, rw_id DESC) 
    WHERE rw_is_deleted = false;

-- Member lookup index for channel access validation
CREATE INDEX rw_idx_channel_members_user 
    ON rw_channel_members (rw_user_id, rw_channel_id);

-- Vector Search Index (HNSW for fast cosine similarity search in AI RAG)
CREATE INDEX rw_idx_messages_embedding_hnsw 
    ON rw_messages USING hnsw (rw_embedding vector_cosine_ops) 
    WHERE rw_is_deleted = false;
