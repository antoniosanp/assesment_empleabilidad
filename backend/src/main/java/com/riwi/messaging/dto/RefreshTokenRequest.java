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
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhMGVlYmM5OS05YzBiLTRlZjgtYmI2ZC02YmI5YmQzODBhMTEiLCJ0eXBlIjoiUkVGUkVTSCIsImlhdCI6MTY5MzEyMzQ1Nn0.token", description = "Valid refresh token string")
    private String refreshToken;
}
