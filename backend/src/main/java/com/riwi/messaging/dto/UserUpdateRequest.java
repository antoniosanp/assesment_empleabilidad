package com.riwi.messaging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Schema(example = "Admin Sistema", description = "User full name")
    private String fullName;

    @Schema(example = "Chief Information Security Officer", description = "User job title")
    private String jobTitle;

    @Schema(example = "true", description = "User active status")
    private Boolean isActive;
}
