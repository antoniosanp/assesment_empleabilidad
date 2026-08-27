package com.riwi.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchDTO {
    private Long messageId;
    private UUID channelId;
    private String channelName;
    private String senderName;
    private String content;
    private String highlightedSnippet;
    private OffsetDateTime createdAt;
}
