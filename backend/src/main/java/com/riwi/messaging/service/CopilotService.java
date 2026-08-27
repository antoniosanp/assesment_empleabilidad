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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CopilotService {

    public static final String SYSTEM_PROMPT_VERSION = "v1.0-RAG-StrictSecurity";
    public static final String SYSTEM_PROMPT = """
        Eres el Copiloto de IA oficial para la plataforma interna de mensajería de Riwi Co. S.A.S.
        REGLAS INVIOLABLES DE SEGURIDAD Y HONESTIDAD:
        1. Responde ÚNICAMENTE utilizando el contexto de mensajes permitidos proporcionado.
        2. Conoces al usuario autenticado, su nombre y su cargo.
        3. Si la pregunta está fuera del alcance del contexto o el usuario no tiene permisos sobre el canal relevante, DEBES NEGARTES EXPLÍCITAMENTE diciendo:
           "No poseo permisos o contexto suficiente en tus canales autorizados para responder esta consulta."
        4. Cita siempre los mensajes fuente y responde con absoluta honestidad.
        """;

    private final MessageRepository messageRepository;
    private final CopilotUsageLogRepository copilotUsageLogRepository;
    private final UserRepository userRepository;
    private final AiProviderService aiProviderService;

    @Transactional
    public CopilotResponseDTO processQuery(CopilotQueryRequest request, UserPrincipal currentUser) {
        String query = request.getQuery().trim();

        // Retrieve RAG context from SQL (RLS-enforced: only channels where currentUser is member)
        List<CopilotContextProjection> contextMessages = messageRepository.findCopilotContextTextFallback(
                currentUser.getId(), query, 5
        );

        // Check for explicit refusal when no context is found
        if (contextMessages.isEmpty()) {
            String refusalMessage = "No poseo permisos o contexto suficiente en tus canales autorizados para responder esta consulta.";
            
            saveUsageLog(currentUser.getId(), query, refusalMessage, 10);

            return CopilotResponseDTO.builder()
                    .answer(refusalMessage)
                    .citations(List.of())
                    .tokensUsed(10)
                    .isRefusedDueToPermissionsOrContext(true)
                    .build();
        }

        // Generate response using interchangeable AI provider (Google AI Studio / Gemini / Mock)
        AiProviderService.AiCompletionResult result = aiProviderService.generateCompletion(
                SYSTEM_PROMPT,
                query,
                contextMessages,
                currentUser
        );

        // Build source citations
        List<CopilotSourceCitationDTO> citations = contextMessages.stream()
                .map(msg -> CopilotSourceCitationDTO.builder()
                        .messageId(msg.getMessageId())
                        .channelId(msg.getChannelId())
                        .channelName(msg.getChannelName())
                        .senderName(msg.getSenderName())
                        .contentSnippet(msg.getContent())
                        .build())
                .collect(Collectors.toList());

        // Save audit log
        saveUsageLog(currentUser.getId(), query, result.answer(), result.tokensUsed());

        return CopilotResponseDTO.builder()
                .answer(result.answer())
                .citations(citations)
                .tokensUsed(result.tokensUsed())
                .isRefusedDueToPermissionsOrContext(false)
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
                        .lastQueryAt(p.getLastQueryAt())
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
