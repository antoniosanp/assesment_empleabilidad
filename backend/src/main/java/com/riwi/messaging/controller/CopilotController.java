package com.riwi.messaging.controller;

import com.riwi.messaging.dto.CopilotQueryRequest;
import com.riwi.messaging.dto.CopilotResponseDTO;
import com.riwi.messaging.dto.CopilotUsageDTO;
import com.riwi.messaging.security.UserPrincipal;
import com.riwi.messaging.service.CopilotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/copilot")
@RequiredArgsConstructor
@Tag(name = "AI Copilot", description = "Endpoints for AI RAG queries with source citations and token consumption audit")
public class CopilotController {

    private final CopilotService copilotService;

    @PostMapping("/query")
    @Operation(summary = "Execute AI Copilot RAG query", description = "Queries AI Copilot using RAG context strictly retrieved from PostgreSQL rw_fn_get_copilot_context. Includes citations and explicit refusals")
    public ResponseEntity<CopilotResponseDTO> processQuery(
            @Valid @RequestBody CopilotQueryRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(copilotService.processQuery(request, currentUser));
    }

    @GetMapping("/usage")
    @Operation(summary = "Get AI Copilot token usage audit", description = "Invokes SQL function rw_fn_get_copilot_usage_by_user to audit total token consumption per user")
    public ResponseEntity<List<CopilotUsageDTO>> getCopilotUsage(
            @RequestParam(required = false) UUID userId
    ) {
        return ResponseEntity.ok(copilotService.getCopilotUsage(userId));
    }
}
