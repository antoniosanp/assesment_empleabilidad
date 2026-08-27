package com.riwi.messaging.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rw_copilot_usage_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopilotUsageLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rw_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rw_user_id", nullable = false, foreignKey = @ForeignKey(name = "rw_fk_copilot_logs_user"))
    @ToString.Exclude
    private UserEntity user;

    @Column(name = "rw_prompt", nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "rw_response", nullable = false, columnDefinition = "TEXT")
    private String response;

    @Column(name = "rw_tokens_used", nullable = false)
    @Builder.Default
    private Integer tokensUsed = 0;

    @Column(name = "rw_created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
