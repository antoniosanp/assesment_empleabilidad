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
public class MessageDTO {
    private Long id;
    private UUID channelId;
    private UUID senderId;
    private String senderName;
    private String content;
    private String status;
    private Boolean isEdited;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
}
