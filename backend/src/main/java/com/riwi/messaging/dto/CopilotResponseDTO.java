package com.riwi.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopilotResponseDTO {
    private String answer;
    private List<CopilotSourceCitationDTO> citations;
    private Integer tokensUsed;
    private Boolean isRefusedDueToPermissionsOrContext;
}
