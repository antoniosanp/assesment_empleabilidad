package com.riwi.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopilotSourceCitationDTO {
    private Long messageId;
    private UUID channelId;
    private String channelName;
    private String senderName;
    private String contentSnippet;
}
