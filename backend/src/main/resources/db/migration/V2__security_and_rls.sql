-- =============================================================================
-- Migration V2: Row Level Security (RLS), Views, Stored Procedures, and Functions
-- Database Engine: PostgreSQL 15+
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Helper Function: Set Session User Context for RLS
-- Sets 'app.current_user_id' for the current transaction scope.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION rw_fn_set_current_user_id(p_user_id UUID)
RETURNS VOID AS $$
BEGIN
    PERFORM set_config('app.current_user_id', p_user_id::TEXT, true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- -----------------------------------------------------------------------------
-- 2. Row Level Security (RLS) Configuration
-- Enable RLS on channels and messages tables
-- -----------------------------------------------------------------------------
ALTER TABLE rw_channels ENABLE ROW LEVEL SECURITY;
ALTER TABLE rw_messages ENABLE ROW LEVEL SECURITY;

-- RLS Policy for rw_channels:
-- Users can view public channels OR private/direct channels where they are active members.
CREATE POLICY rw_pol_channels_select ON rw_channels
    FOR SELECT
    USING (
        rw_type = 'PUBLIC'
        OR rw_id IN (
            SELECT cm.rw_channel_id 
            FROM rw_channel_members cm
            WHERE cm.rw_user_id = NULLIF(current_setting('app.current_user_id', true), '')::UUID
        )
    );

-- RLS Policy for rw_messages:
-- Users can read messages only in channels where they are members.
CREATE POLICY rw_pol_messages_select ON rw_messages
    FOR SELECT
    USING (
        rw_is_deleted = false
        AND rw_channel_id IN (
            SELECT cm.rw_channel_id 
            FROM rw_channel_members cm
            WHERE cm.rw_user_id = NULLIF(current_setting('app.current_user_id', true), '')::UUID
        )
    );

-- RLS Policy for inserting rw_messages:
-- Users can insert messages only in channels where they are members and as themselves.
CREATE POLICY rw_pol_messages_insert ON rw_messages
    FOR INSERT
    WITH CHECK (
        rw_sender_id = NULLIF(current_setting('app.current_user_id', true), '')::UUID
        AND rw_channel_id IN (
            SELECT cm.rw_channel_id 
            FROM rw_channel_members cm
            WHERE cm.rw_user_id = NULLIF(current_setting('app.current_user_id', true), '')::UUID
        )
    );

-- -----------------------------------------------------------------------------
-- 3. User Conversations View
-- Shows user channels, last message sent/received, unread count and channel metadata.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW rw_v_user_conversations AS
SELECT 
    c.rw_id AS rw_channel_id,
    c.rw_name AS rw_channel_name,
    c.rw_type AS rw_channel_type,
    cm.rw_user_id AS rw_user_id,
    cm.rw_member_role AS rw_member_role,
    cm.rw_last_read_at AS rw_last_read_at,
    lm.rw_id AS rw_last_message_id,
    lm.rw_content AS rw_last_message_content,
    lm.rw_created_at AS rw_last_message_at,
    lm.rw_sender_id AS rw_last_message_sender_id,
    (
        SELECT COUNT(*) 
        FROM rw_messages m 
        WHERE m.rw_channel_id = c.rw_id 
          AND m.rw_created_at > cm.rw_last_read_at
          AND m.rw_is_deleted = false
    ) AS rw_unread_count
FROM rw_channels c
INNER JOIN rw_channel_members cm ON c.rw_id = cm.rw_channel_id
LEFT JOIN LATERAL (
    SELECT m.rw_id, m.rw_content, m.rw_created_at, m.rw_sender_id
    FROM rw_messages m
    WHERE m.rw_channel_id = c.rw_id AND m.rw_is_deleted = false
    ORDER BY m.rw_created_at DESC, m.rw_id DESC
    LIMIT 1
) lm ON true;

-- -----------------------------------------------------------------------------
-- 4. Stored Procedure 1: User Search & Query
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE rw_sp_get_users(
    IN p_search_term TEXT,
    IN p_role_filter TEXT,
    INOUT p_result_ref REFCURSOR
)
LANGUAGE plpgsql AS $$
BEGIN
    OPEN p_result_ref FOR
    SELECT 
        rw_id,
        rw_email,
        rw_full_name,
        rw_job_title,
        rw_role,
        rw_is_active,
        rw_created_at
    FROM rw_users
    WHERE (p_search_term IS NULL OR rw_full_name ILIKE '%' || p_search_term || '%' OR rw_email ILIKE '%' || p_search_term || '%')
      AND (p_role_filter IS NULL OR rw_role = p_role_filter)
    ORDER BY rw_full_name ASC;
END;
$$;

-- -----------------------------------------------------------------------------
-- 5. Stored Procedure 2: Edit & Soft Delete User
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE rw_sp_update_or_delete_user(
    IN p_user_id UUID,
    IN p_full_name TEXT,
    IN p_job_title TEXT,
    IN p_is_active BOOLEAN
)
LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM rw_users WHERE rw_id = p_user_id) THEN
        RAISE EXCEPTION 'User with ID % does not exist', p_user_id;
    END IF;

    UPDATE rw_users
    SET 
        rw_full_name = COALESCE(p_full_name, rw_full_name),
        rw_job_title = COALESCE(p_job_title, rw_job_title),
        rw_is_active = COALESCE(p_is_active, rw_is_active),
        rw_updated_at = (now() AT TIME ZONE 'utc')
    WHERE rw_id = p_user_id;
END;
$$;

-- -----------------------------------------------------------------------------
-- 6. Required SQL Functions (Section 11 in Assessment PDF)
-- -----------------------------------------------------------------------------

-- Consulta 1: Historial de mensajes de un canal con paginación por keyset
CREATE OR REPLACE FUNCTION rw_fn_get_channel_messages(
    p_channel_id UUID,
    p_after_id BIGINT DEFAULT 0,
    p_limit INT DEFAULT 30
)
RETURNS TABLE (
    rw_message_id BIGINT,
    rw_channel_id UUID,
    rw_sender_id UUID,
    rw_sender_name VARCHAR,
    rw_content TEXT,
    rw_status VARCHAR,
    rw_is_edited BOOLEAN,
    rw_created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        m.rw_id AS rw_message_id,
        m.rw_channel_id,
        m.rw_sender_id,
        u.rw_full_name AS rw_sender_name,
        m.rw_content,
        m.rw_status,
        m.rw_is_edited,
        m.rw_created_at
    FROM rw_messages m
    INNER JOIN rw_users u ON m.rw_sender_id = u.rw_id
    WHERE m.rw_channel_id = p_channel_id
      AND m.rw_is_deleted = false
      AND (p_after_id = 0 OR m.rw_id > p_after_id)
    ORDER BY m.rw_id ASC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql STABLE;

-- Consulta 2: Búsqueda de mensajes con resaltado del término encontrado
CREATE OR REPLACE FUNCTION rw_fn_search_messages(
    p_user_id UUID,
    p_search_term TEXT
)
RETURNS TABLE (
    rw_message_id BIGINT,
    rw_channel_id UUID,
    rw_channel_name VARCHAR,
    rw_sender_name VARCHAR,
    rw_content TEXT,
    rw_highlighted_snippet TEXT,
    rw_created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        m.rw_id AS rw_message_id,
        m.rw_channel_id,
        c.rw_name AS rw_channel_name,
        u.rw_full_name AS rw_sender_name,
        m.rw_content,
        regexp_replace(
            m.rw_content, 
            '(' || regexp_replace(p_search_term, '([.*+?^$()|[\]\\])', '\\\1', 'g') || ')', 
            '<mark>\1</mark>', 
            'gi'
        ) AS rw_highlighted_snippet,
        m.rw_created_at
    FROM rw_messages m
    INNER JOIN rw_channels c ON m.rw_channel_id = c.rw_id
    INNER JOIN rw_users u ON m.rw_sender_id = u.rw_id
    INNER JOIN rw_channel_members cm ON c.rw_id = cm.rw_channel_id
    WHERE cm.rw_user_id = p_user_id
      AND m.rw_is_deleted = false
      AND m.rw_content ILIKE '%' || p_search_term || '%'
    ORDER BY m.rw_created_at DESC;
END;
$$ LANGUAGE plpgsql STABLE;

-- Consulta 3: Recuperación de contexto para el copiloto con permisos estrictos en SQL
CREATE OR REPLACE FUNCTION rw_fn_get_copilot_context(
    p_user_id UUID,
    p_query_embedding vector(1536),
    p_match_count INT DEFAULT 5
)
RETURNS TABLE (
    rw_message_id BIGINT,
    rw_channel_id UUID,
    rw_channel_name VARCHAR,
    rw_sender_name VARCHAR,
    rw_sender_job_title VARCHAR,
    rw_content TEXT,
    rw_created_at TIMESTAMPTZ,
    rw_similarity FLOAT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        m.rw_id AS rw_message_id,
        m.rw_channel_id,
        c.rw_name AS rw_channel_name,
        u.rw_full_name AS rw_sender_name,
        u.rw_job_title AS rw_sender_job_title,
        m.rw_content,
        m.rw_created_at,
        1 - (m.rw_embedding <=> p_query_embedding) AS rw_similarity
    FROM rw_messages m
    INNER JOIN rw_channels c ON m.rw_channel_id = c.rw_id
    INNER JOIN rw_users u ON m.rw_sender_id = u.rw_id
    INNER JOIN rw_channel_members cm ON c.rw_id = cm.rw_channel_id
    WHERE cm.rw_user_id = p_user_id
      AND m.rw_is_deleted = false
      AND m.rw_embedding IS NOT NULL
    ORDER BY m.rw_embedding <=> p_query_embedding ASC
    LIMIT p_match_count;
END;
$$ LANGUAGE plpgsql STABLE;

-- Consulta 4: Consumo acumulado del copiloto por usuario
CREATE OR REPLACE FUNCTION rw_fn_get_copilot_usage_by_user(
    p_user_id UUID DEFAULT NULL
)
RETURNS TABLE (
    rw_user_id UUID,
    rw_full_name VARCHAR,
    rw_job_title VARCHAR,
    rw_total_queries BIGINT,
    rw_total_tokens_used BIGINT,
    rw_last_query_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.rw_id AS rw_user_id,
        u.rw_full_name,
        u.rw_job_title,
        COUNT(l.rw_id)::BIGINT AS rw_total_queries,
        COALESCE(SUM(l.rw_tokens_used), 0)::BIGINT AS rw_total_tokens_used,
        MAX(l.rw_created_at) AS rw_last_query_at
    FROM rw_users u
    LEFT JOIN rw_copilot_usage_logs l ON u.rw_id = l.rw_user_id
    WHERE (p_user_id IS NULL OR u.rw_id = p_user_id)
    GROUP BY u.rw_id, u.rw_full_name, u.rw_job_title
    ORDER BY rw_total_tokens_used DESC;
END;
$$ LANGUAGE plpgsql STABLE;

-- -----------------------------------------------------------------------------
-- 7. Trigger: Update Timestamp Consistency
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION rw_fn_trg_update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.rw_updated_at = (now() AT TIME ZONE 'utc');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER rw_trg_users_updated_at
    BEFORE UPDATE ON rw_users
    FOR EACH ROW EXECUTE FUNCTION rw_fn_trg_update_timestamp();

CREATE TRIGGER rw_trg_channels_updated_at
    BEFORE UPDATE ON rw_channels
    FOR EACH ROW EXECUTE FUNCTION rw_fn_trg_update_timestamp();

CREATE TRIGGER rw_trg_messages_updated_at
    BEFORE UPDATE ON rw_messages
    FOR EACH ROW EXECUTE FUNCTION rw_fn_trg_update_timestamp();
