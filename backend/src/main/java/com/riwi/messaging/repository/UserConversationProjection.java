package com.riwi.messaging.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface UserConversationProjection {
    UUID getChannelId();
    String getChannelName();
    String getChannelType();
    UUID getUserId();
    String getMemberRole();
    OffsetDateTime getLastReadAt();
    Long getLastMessageId();
    String getLastMessageContent();
    OffsetDateTime getLastMessageAt();
    UUID getLastMessageSenderId();
    Long getUnreadCount();
}
