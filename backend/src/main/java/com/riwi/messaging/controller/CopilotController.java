package com.riwi.messaging.controller;

import com.riwi.messaging.dto.CopilotQueryRequest;
import com.riwi.messaging.dto.CopilotResponseDTO;
import com.riwi.messaging.dto.CopilotUsageDTO;
import com.riwi.messaging.security.UserPrincipal;
import com.riwi.messaging.service.CopilotService;
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
public class CopilotController {

    private final CopilotService copilotService;

    @PostMapping("/query")
    public ResponseEntity<CopilotResponseDTO> processQuery(
            @Valid @RequestBody CopilotQueryRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(copilotService.processQuery(request, currentUser));
    }

    @GetMapping("/usage")
    public ResponseEntity<List<CopilotUsageDTO>> getCopilotUsage(
            @RequestParam(required = false) UUID userId
    ) {
        return ResponseEntity.ok(copilotService.getCopilotUsage(userId));
    }
}
