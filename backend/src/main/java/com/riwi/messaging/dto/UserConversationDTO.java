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
public class UserConversationDTO {
    private UUID channelId;
    private String channelName;
    private String channelType;
    private UUID userId;
    private String memberRole;
    private OffsetDateTime lastReadAt;
    private Long lastMessageId;
    private String lastMessageContent;
    private OffsetDateTime lastMessageAt;
    private UUID lastMessageSenderId;
    private Long unreadCount;
}
