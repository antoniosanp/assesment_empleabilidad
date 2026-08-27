package com.riwi.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChannelRequest {

    @Schema(example = "Desarrollo Frontend", description = "Name of the channel")
    private String name;

    @NotBlank(message = "Channel type is required (PUBLIC, PRIVATE, DIRECT)")
    @Schema(example = "PRIVATE", description = "Channel type: PUBLIC, PRIVATE, DIRECT")
    private String type;

    @Schema(example = "[\"b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22\", \"c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33\"]", description = "List of member user UUIDs")
    private List<UUID> memberUserIds;
}
