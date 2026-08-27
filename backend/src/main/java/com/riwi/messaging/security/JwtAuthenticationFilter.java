package com.riwi.messaging.security;

import com.riwi.messaging.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String token = parseJwt(request);

        if (token != null && jwtUtil.validateToken(token)) {
            String tokenType = jwtUtil.extractTokenType(token);

            if ("ACCESS".equals(tokenType)) {
                UUID userId = jwtUtil.extractUserId(token);

                userRepository.findById(userId).ifPresent(user -> {
                    if (Boolean.TRUE.equals(user.getIsActive())) {
                        UserPrincipal principal = UserPrincipal.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .passwordHash(user.getPasswordHash())
                                .fullName(user.getFullName())
                                .jobTitle(user.getJobTitle())
                                .role(user.getRole())
                                .isActive(user.getIsActive())
                                .build();

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                });
            }
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        String paramAuth = request.getParameter("token");
        if (StringUtils.hasText(paramAuth)) {
            return paramAuth;
        }

        return null;
    }
}
