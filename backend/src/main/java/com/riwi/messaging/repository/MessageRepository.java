package com.riwi.messaging.repository;

import com.riwi.messaging.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query(value = """
        SELECT 
            rw_message_id AS messageId,
            rw_channel_id AS channelId,
            rw_sender_id AS senderId,
            rw_sender_name AS senderName,
            rw_content AS content,
            rw_status AS status,
            rw_is_edited AS isEdited,
            rw_created_at AS createdAt
        FROM rw_fn_get_channel_messages(:channelId, :afterId, :limit)
        """, nativeQuery = true)
    List<ChannelMessageProjection> findChannelMessagesKeyset(
            @Param("channelId") UUID channelId,
            @Param("afterId") Long afterId,
            @Param("limit") Integer limit
    );

    @Query(value = """
        SELECT 
            rw_message_id AS messageId,
            rw_channel_id AS channelId,
            rw_channel_name AS channelName,
            rw_sender_name AS senderName,
            rw_content AS content,
            rw_highlighted_snippet AS highlightedSnippet,
            rw_created_at AS createdAt
        FROM rw_fn_search_messages(:userId, :searchTerm)
        """, nativeQuery = true)
    List<MessageSearchProjection> searchMessagesWithHighlight(
            @Param("userId") UUID userId,
            @Param("searchTerm") String searchTerm
    );

    @Query(value = """
        SELECT 
            rw_message_id AS messageId,
            rw_channel_id AS channelId,
            rw_channel_name AS channelName,
            rw_sender_name AS senderName,
            rw_sender_job_title AS senderJobTitle,
            rw_content AS content,
            rw_created_at AS createdAt,
            rw_similarity AS similarity
        FROM rw_fn_get_copilot_context(:userId, CAST(:embedding AS vector), :matchCount)
        """, nativeQuery = true)
    List<CopilotContextProjection> findCopilotContextVector(
            @Param("userId") UUID userId,
            @Param("embedding") String embeddingVectorString,
            @Param("matchCount") Integer matchCount
    );

    @Query(value = """
        SELECT 
            m.rw_id AS messageId,
            m.rw_channel_id AS channelId,
            c.rw_name AS channelName,
            u.rw_full_name AS senderName,
            u.rw_job_title AS senderJobTitle,
            m.rw_content AS content,
            m.rw_created_at AS createdAt,
            1.0 AS similarity
        FROM rw_messages m
        INNER JOIN rw_channels c ON m.rw_channel_id = c.rw_id
        INNER JOIN rw_users u ON m.rw_sender_id = u.rw_id
        INNER JOIN rw_channel_members cm ON c.rw_id = cm.rw_channel_id
        WHERE cm.rw_user_id = :userId
          AND m.rw_is_deleted = false
          AND (:queryText IS NULL OR :queryText = :queryText)
        ORDER BY m.rw_created_at DESC, m.rw_id DESC
        LIMIT :matchCount
        """, nativeQuery = true)
    List<CopilotContextProjection> findCopilotContextTextFallback(
            @Param("userId") UUID userId,
            @Param("queryText") String queryText,
            @Param("matchCount") Integer matchCount
    );
}
