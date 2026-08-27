package com.riwi.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "Channel ID is required")
    @Schema(example = "11111111-1111-1111-1111-111111111111", description = "Target channel UUID")
    private UUID channelId;

    @NotBlank(message = "Message content cannot be blank")
    @Schema(example = "Hola equipo, confirmo la revisión de la plataforma de mensajería.", description = "Message text content")
    private String content;
}
