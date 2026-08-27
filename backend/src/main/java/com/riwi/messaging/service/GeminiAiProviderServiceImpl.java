package com.riwi.messaging.service;

import com.riwi.messaging.repository.CopilotContextProjection;
import com.riwi.messaging.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "copilot.provider", havingValue = "gemini", matchIfMissing = true)
@Slf4j
public class GeminiAiProviderServiceImpl implements AiProviderService {

    private final String apiKey;
    private final String defaultModelName;
    private final RestClient restClient;

    public GeminiAiProviderServiceImpl(
            @Value("${gemini.api-key:${openai.api-key:}}") String apiKey,
            @Value("${gemini.model:${openai.model:gemini-3.6-flash}}") String modelName
    ) {
        String effectiveKey = apiKey;
        if (effectiveKey == null || effectiveKey.isBlank() || effectiveKey.contains("your_")) {
            effectiveKey = System.getProperty("OPENAI_API_KEY", System.getenv("OPENAI_API_KEY"));
        }
        if (effectiveKey == null || effectiveKey.isBlank()) {
            effectiveKey = readKeyFromEnvFile();
        }

        this.apiKey = effectiveKey;
        this.defaultModelName = (modelName != null && !modelName.isBlank()) ? modelName : "gemini-3.6-flash";
        this.restClient = RestClient.builder().build();

        if (this.apiKey != null && !this.apiKey.isBlank()) {
            log.info("GeminiAiProviderServiceImpl initialized with LIVE Google AI Studio key (Default Model: {})", this.defaultModelName);
        } else {
            log.warn("Gemini API key is missing. AI queries will use fallback mock.");
        }
    }

    private static String readKeyFromEnvFile() {
        try {
            Path envPath = Path.of(".env");
            if (!Files.exists(envPath)) {
                envPath = Path.of("../.env");
            }
            if (Files.exists(envPath)) {
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("OPENAI_API_KEY=") || line.startsWith("GEMINI_API_KEY=")) {
                        return line.substring(line.indexOf('=') + 1).trim();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not read API key from .env file: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String getProviderName() {
        return "Google AI Studio (Gemini - " + defaultModelName + ")";
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
            log.warn("Gemini API key is not configured in environment or .env. Falling back to mock response.");
            return new AiCompletionResult(
                    "Respuesta de prueba (Gemini AI Studio): Basado en los " + contextMessages.size() + " mensajes recuperados para " + authenticatedUser.getFullName() + ", confirmo la consulta sobre: " + userQuery,
                    150
            );
        }

        // List of candidate models to try in sequence if 503 high demand occurs
        List<String> modelsToTry = List.of(defaultModelName, "gemini-2.5-flash", "gemini-1.5-flash");
        Exception lastException = null;

        for (String targetModel : modelsToTry) {
            try {
                log.info("Sending live query to Google AI Studio Gemini API (Model: {})", targetModel);
                String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", targetModel, apiKey);

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
                lastException = e;
                log.warn("Model {} failed with error: {}. Trying fallback model if available...", targetModel, e.getMessage());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }
        }

        log.error("All Gemini models failed: {}", lastException != null ? lastException.getMessage() : "Unknown error", lastException);
        return new AiCompletionResult(
                "Google AI Studio está experimentando una alta demanda temporal en sus servidores (Error 503 Service Unavailable). Por favor intenta de nuevo en unos segundos.",
                0
        );
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
