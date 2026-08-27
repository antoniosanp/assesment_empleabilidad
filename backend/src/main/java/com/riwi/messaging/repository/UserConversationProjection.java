package com.riwi.messaging.repository;

import java.time.Instant;
import java.util.UUID;

public interface UserConversationProjection {
    UUID getChannelId();
    String getChannelName();
    String getChannelType();
    UUID getUserId();
    String getMemberRole();
    Instant getLastReadAt();
    Long getLastMessageId();
    String getLastMessageContent();
    Instant getLastMessageAt();
    UUID getLastMessageSenderId();
    Long getUnreadCount();
}
