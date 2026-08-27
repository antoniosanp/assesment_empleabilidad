package com.riwi.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopilotQueryRequest {

    @NotBlank(message = "Prompt query cannot be blank")
    @Schema(example = "¿Qué avances se reportaron sobre la migración a PostgreSQL?", description = "Natural language question for AI Copilot RAG search")
    private String query;
}
