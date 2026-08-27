package com.riwi.messaging.service;

import com.riwi.messaging.repository.CopilotContextProjection;
import com.riwi.messaging.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "copilot.provider", havingValue = "gemini", matchIfMissing = false)
@Slf4j
public class GeminiAiProviderServiceImpl implements AiProviderService {

    private final String apiKey;
    private final String modelName;
    private final RestClient restClient;

    public GeminiAiProviderServiceImpl(
            @Value("${gemini.api-key:${openai.api-key:}}") String apiKey,
            @Value("${gemini.model:gemini-1.5-flash}") String modelName
    ) {
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public String getProviderName() {
        return "Google AI Studio (Gemini - " + modelName + ")";
    }

    @Override
    public AiCompletionResult generateCompletion(
            String systemPrompt,
            String userQuery,
            List<CopilotContextProjection> contextMessages,
            UserPrincipal authenticatedUser
    ) {
        StringBuilder contextText = new StringBuilder();
        for (CopilotContextProjection msg : contextMessages) {
            contextText.append(String.format("- [Mensaje ID #%d en Canal '%s' por %s (%s)]: \"%s\"\n",
                    msg.getMessageId(), msg.getChannelName(), msg.getSenderName(), msg.getSenderJobTitle(), msg.getContent()));
        }

        String fullPrompt = String.format(
                "%s\n\n[USUARIO AUTENTICADO]\nNombre: %s\nCargo: %s\n\n[CONTEXTO DE MENSAJES PERMITIDOS]\n%s\n\n[PREGUNTA DEL USUARIO]\n%s",
                systemPrompt,
                authenticatedUser.getFullName(),
                authenticatedUser.getJobTitle(),
                contextText.length() > 0 ? contextText.toString() : "(Ninguno)",
                userQuery
        );

        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your_")) {
            log.warn("Gemini API key is not configured. Falling back to structured response.");
            return new AiCompletionResult(
                    "Respuesta de prueba (Gemini AI Studio): Basado en los " + contextMessages.size() + " mensajes recuperados para " + authenticatedUser.getFullName() + ", confirmo la consulta sobre: " + userQuery,
                    150
            );
        }

        try {
            String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", modelName, apiKey);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", fullPrompt)))
                    )
            );

            Map<?, ?> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            String answerText = extractTextFromGeminiResponse(response);
            return new AiCompletionResult(answerText, calculateApproxTokens(fullPrompt + answerText));

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return new AiCompletionResult(
                    "Error al comunicarse con Google AI Studio. Error: " + e.getMessage(),
                    0
            );
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromGeminiResponse(Map<?, ?> response) {
        try {
            List<Map<?, ?>> candidates = (List<Map<?, ?>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<?, ?> content = (Map<?, ?>) candidates.get(0).get("content");
                List<Map<?, ?>> parts = (List<Map<?, ?>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            log.error("Parsing Gemini response failed", e);
        }
        return "No se obtuvo respuesta del modelo Gemini.";
    }

    private int calculateApproxTokens(String text) {
        return text == null ? 0 : text.length() / 4;
    }
}
