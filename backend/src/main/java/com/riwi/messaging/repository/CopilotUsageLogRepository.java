package com.riwi.messaging.repository;

import com.riwi.messaging.model.CopilotUsageLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CopilotUsageLogRepository extends JpaRepository<CopilotUsageLogEntity, Long> {

    @Query(value = """
        SELECT 
            rw_user_id AS userId,
            rw_full_name AS fullName,
            rw_job_title AS jobTitle,
            rw_total_queries AS totalQueries,
            rw_total_tokens_used AS totalTokensUsed,
            rw_last_query_at AS lastQueryAt
        FROM rw_fn_get_copilot_usage_by_user(:userId)
        """, nativeQuery = true)
    List<CopilotUsageLogProjection> findCopilotUsageByUser(@Param("userId") UUID userId);

    @Query(value = """
        SELECT 
            rw_user_id AS userId,
            rw_full_name AS fullName,
            rw_job_title AS jobTitle,
            rw_total_queries AS totalQueries,
            rw_total_tokens_used AS totalTokensUsed,
            rw_last_query_at AS lastQueryAt
        FROM rw_fn_get_copilot_usage_by_user(NULL)
        """, nativeQuery = true)
    List<CopilotUsageLogProjection> findAllCopilotUsageSummary();
}
