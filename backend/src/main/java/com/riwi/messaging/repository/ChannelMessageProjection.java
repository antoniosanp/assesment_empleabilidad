package com.riwi.messaging.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ChannelMessageProjection {
    Long getMessageId();
    UUID getChannelId();
    UUID getSenderId();
    String getSenderName();
    String getContent();
    String getStatus();
    Boolean getIsEdited();
    OffsetDateTime getCreatedAt();
}
