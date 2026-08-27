package com.riwi.messaging.repository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface CopilotContextProjection {
    Long getMessageId();
    UUID getChannelId();
    String getChannelName();
    String getSenderName();
    String getSenderJobTitle();
    String getContent();
    OffsetDateTime getCreatedAt();
    Double getSimilarity();
}
