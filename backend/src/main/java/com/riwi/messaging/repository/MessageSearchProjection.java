package com.riwi.messaging.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface MessageSearchProjection {
    Long getMessageId();
    UUID getChannelId();
    String getChannelName();
    String getSenderName();
    String getContent();
    String getHighlightedSnippet();
    OffsetDateTime getCreatedAt();
}
