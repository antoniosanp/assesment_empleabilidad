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
public class KeysetPageResponse<T> {
    private List<T> items;
    private Long nextAfterId;
    private boolean hasMore;
}
