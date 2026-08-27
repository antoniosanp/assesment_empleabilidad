package com.riwi.messaging.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@Slf4j
public class PostgresRlsAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("@annotation(org.springframework.transaction.annotation.Transactional) || within(@org.springframework.transaction.annotation.Transactional *)")
    public void setPostgresUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            UUID userId = principal.getId();
            try {
                entityManager.createNativeQuery("SELECT rw_fn_set_current_user_id(:userId)")
                        .setParameter("userId", userId)
                        .getSingleResult();
                log.debug("PostgreSQL RLS app.current_user_id set to {}", userId);
            } catch (Exception e) {
                log.error("Failed to set PostgreSQL RLS user context for userId: {}", userId, e);
            }
        }
    }
}
