package com.riwi.messaging.service;

import com.riwi.messaging.dto.CopilotQueryRequest;
import com.riwi.messaging.dto.CopilotResponseDTO;
import com.riwi.messaging.dto.CopilotSourceCitationDTO;
import com.riwi.messaging.dto.CopilotUsageDTO;
import com.riwi.messaging.model.CopilotUsageLogEntity;
import com.riwi.messaging.model.UserEntity;
import com.riwi.messaging.repository.CopilotContextProjection;
import com.riwi.messaging.repository.CopilotUsageLogRepository;
import com.riwi.messaging.repository.MessageRepository;
import com.riwi.messaging.repository.UserRepository;
import com.riwi.messaging.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CopilotService {

    public static final String SYSTEM_PROMPT_VERSION = "v1.1-RAG-IntelligentContext";
    public static final String SYSTEM_PROMPT = """
        Eres el Copiloto de IA oficial para la plataforma interna de mensajería de Riwi Co. S.A.S.
        REGLAS DE ACTUACIÓN:
        1. Tu función principal es responder preguntas del usuario basándote en la información de la plataforma y sus conversaciones en canales autorizados.
        2. Conoces al usuario autenticado (nombre y cargo) y únicamente se te proporciona el contexto de los canales a los que él pertenece.
        3. Si el usuario hace preguntas generales, saludos o consultas sencillas (ej. "¿cuánto es 2+2?", "¿quién eres?"), responde amablemente de forma concisa.
        4. Si el usuario pregunta específicamente por información privada, temas o proyectos de canales a los que NO pertenece o sobre los que no se te ha proporcionado contexto en sus canales autorizados, DEBES responder explícitamente:
           "No poseo permisos o contexto suficiente en tus canales autorizados para responder esta consulta."
        5. Cita siempre los mensajes fuente (Mensaje ID y Canal) cuando tu respuesta se base en las conversaciones del equipo.
        """;

    private final MessageRepository messageRepository;
    private final CopilotUsageLogRepository copilotUsageLogRepository;
    private final UserRepository userRepository;
    private final AiProviderService aiProviderService;

    @Transactional
    public CopilotResponseDTO processQuery(CopilotQueryRequest request, UserPrincipal currentUser) {
        String query = request.getQuery().trim();

        // Retrieve RAG context from SQL (RLS-enforced: top 20 recent messages from user's authorized channels)
        List<CopilotContextProjection> contextMessages = messageRepository.findCopilotContextTextFallback(
                currentUser.getId(), query, 20
        );

        // Generate intelligent completion with Gemini AI Studio
        AiProviderService.AiCompletionResult result = aiProviderService.generateCompletion(
                SYSTEM_PROMPT,
                query,
                contextMessages,
                currentUser
        );

        boolean isRefused = result.answer().contains("No poseo permisos") || result.answer().contains("insuficiente");

        // Build source citations (only include citations if not refused and context was referenced)
        List<CopilotSourceCitationDTO> citations = isRefused || contextMessages.isEmpty()
                ? List.of()
                : contextMessages.stream()
                .filter(msg -> result.answer().contains(msg.getChannelName()) || result.answer().contains(String.valueOf(msg.getMessageId())) || result.answer().toLowerCase().contains(msg.getSenderName().toLowerCase()))
                .map(msg -> CopilotSourceCitationDTO.builder()
                        .messageId(msg.getMessageId())
                        .channelId(msg.getChannelId())
                        .channelName(msg.getChannelName())
                        .senderName(msg.getSenderName())
                        .contentSnippet(msg.getContent())
                        .build())
                .collect(Collectors.toList());

        // Fallback: If no specific citations matched text but answer used context and wasn't refused, include top 2 context items
        if (citations.isEmpty() && !isRefused && !contextMessages.isEmpty() && !query.toLowerCase().contains("2+") && !query.toLowerCase().contains("cuanto es")) {
            citations = contextMessages.stream()
                    .limit(2)
                    .map(msg -> CopilotSourceCitationDTO.builder()
                            .messageId(msg.getMessageId())
                            .channelId(msg.getChannelId())
                            .channelName(msg.getChannelName())
                            .senderName(msg.getSenderName())
                            .contentSnippet(msg.getContent())
                            .build())
                    .collect(Collectors.toList());
        }

        // Save audit log
        saveUsageLog(currentUser.getId(), query, result.answer(), result.tokensUsed());

        return CopilotResponseDTO.builder()
                .answer(result.answer())
                .citations(citations)
                .tokensUsed(result.tokensUsed())
                .isRefusedDueToPermissionsOrContext(isRefused)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CopilotUsageDTO> getCopilotUsage(UUID userIdFilter) {
        var logs = (userIdFilter != null) 
                ? copilotUsageLogRepository.findCopilotUsageByUser(userIdFilter)
                : copilotUsageLogRepository.findAllCopilotUsageSummary();

        return logs.stream()
                .map(p -> CopilotUsageDTO.builder()
                        .userId(p.getUserId())
                        .fullName(p.getFullName())
                        .jobTitle(p.getJobTitle())
                        .totalQueries(p.getTotalQueries())
                        .totalTokensUsed(p.getTotalTokensUsed())
                        .lastQueryAt(p.getLastQueryAt() != null ? p.getLastQueryAt().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .collect(Collectors.toList());
    }

    private void saveUsageLog(UUID userId, String prompt, String response, int tokensUsed) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            CopilotUsageLogEntity logEntity = CopilotUsageLogEntity.builder()
                    .user(user)
                    .prompt(prompt)
                    .response(response)
                    .tokensUsed(tokensUsed)
                    .build();
            copilotUsageLogRepository.save(logEntity);
        }
    }
}
