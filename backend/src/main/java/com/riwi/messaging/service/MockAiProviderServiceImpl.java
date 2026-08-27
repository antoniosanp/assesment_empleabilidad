package com.riwi.messaging.service;

import com.riwi.messaging.repository.CopilotContextProjection;
import com.riwi.messaging.security.UserPrincipal;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(name = "copilot.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiProviderServiceImpl implements AiProviderService {

    @Override
    public String getProviderName() {
        return "Mock Internal AI Engine";
    }

    @Override
    public AiCompletionResult generateCompletion(
            String systemPrompt,
            String userQuery,
            List<CopilotContextProjection> contextMessages,
            UserPrincipal authenticatedUser
    ) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(String.format("Hola %s (%s). ", authenticatedUser.getFullName(), authenticatedUser.getJobTitle()));
        responseBuilder.append("Según el contexto de tus canales permitidos: ");

        if (contextMessages.isEmpty()) {
            responseBuilder.append("No encontré información relevante disponible en tus canales.");
        } else {
            responseBuilder.append(String.format("He analizado %d mensaje(s) fuente. ", contextMessages.size()));
            for (CopilotContextProjection msg : contextMessages) {
                responseBuilder.append(String.format("En el canal '%s', %s indicó: \"%s\". ",
                        msg.getChannelName(), msg.getSenderName(), msg.getContent()));
            }
        }

        return new AiCompletionResult(
                responseBuilder.toString(),
                120 + (contextMessages.size() * 35)
        );
    }
}
