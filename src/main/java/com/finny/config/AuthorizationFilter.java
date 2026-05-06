package com.finny.config;

import com.finny.domain.master.UserSession;
import com.finny.repository.master.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class AuthorizationFilter extends OncePerRequestFilter {

    private final UserSessionRepository userSessionRepository;

    public AuthorizationFilter(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        Optional<UserSession> sessionOpt = userSessionRepository.findByTokenAndActiveTrue(token);

        if (sessionOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        UserSession session = sessionOpt.get();

        try {
            // Set context for multitenancy resolving downstream
            UserContext.setCurrentUser(session.getUser().getId());
            UserContext.setTenantId(session.getUser().getTenant().getId());
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
