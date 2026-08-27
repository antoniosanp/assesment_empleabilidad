package com.riwi.messaging.repository;

import java.time.Instant;
import java.util.UUID;

public interface CopilotUsageLogProjection {
    UUID getUserId();
    String getFullName();
    String getJobTitle();
    Long getTotalQueries();
    Long getTotalTokensUsed();
    Instant getLastQueryAt();
}
