package com.riwi.messaging.repository;

import com.riwi.messaging.model.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, UUID> {

    @Query(value = """
        SELECT 
            rw_channel_id AS channelId,
            rw_channel_name AS channelName,
            rw_channel_type AS channelType,
            rw_user_id AS userId,
            rw_member_role AS memberRole,
            rw_last_read_at AS lastReadAt,
            rw_last_message_id AS lastMessageId,
            rw_last_message_content AS lastMessageContent,
            rw_last_message_at AS lastMessageAt,
            rw_last_message_sender_id AS lastMessageSenderId,
            rw_unread_count AS unreadCount
        FROM rw_v_user_conversations
        WHERE rw_user_id = :userId
        ORDER BY rw_last_message_at DESC NULLS LAST
        """, nativeQuery = true)
    List<UserConversationProjection> findUserConversations(@Param("userId") UUID userId);
}
