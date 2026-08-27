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
public class CopilotUsageDTO {
    private UUID userId;
    private String fullName;
    private String jobTitle;
    private Long totalQueries;
    private Long totalTokensUsed;
    private OffsetDateTime lastQueryAt;
}
