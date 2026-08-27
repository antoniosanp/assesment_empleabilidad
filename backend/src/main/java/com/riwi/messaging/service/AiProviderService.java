package com.riwi.messaging.service;

import com.riwi.messaging.repository.CopilotContextProjection;
import com.riwi.messaging.security.UserPrincipal;

import java.util.List;

public interface AiProviderService {
    String getProviderName();

    AiCompletionResult generateCompletion(
            String systemPrompt,
            String userQuery,
            List<CopilotContextProjection> contextMessages,
            UserPrincipal authenticatedUser
    );

    record AiCompletionResult(
            String answer,
            int tokensUsed
    ) {}
}
