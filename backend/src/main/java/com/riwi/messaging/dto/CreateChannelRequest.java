package com.riwi.messaging.dto;

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
    private String name;

    @NotBlank(message = "Channel type is required (PUBLIC, PRIVATE, DIRECT)")
    private String type;

    private List<UUID> memberUserIds;
}
