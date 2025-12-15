package gofish.pt.security;

import gofish.pt.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Profile("!test")  // Only load in non-test environments
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract Authorization header
        final String authHeader = request.getHeader("Authorization");
        
        logger.info("Request to: " + request.getRequestURI());
        logger.info("Authorization header: " + (authHeader != null ? "present" : "missing"));
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.info("No valid authorization header, continuing without authentication");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extract JWT token
            final String jwt = authHeader.substring(7);
            logger.info("JWT token extracted, length: " + jwt.length());
            
            // 3. Validate token and extract userId
            if (jwtService.validateToken(jwt)) {
                Long userId = jwtService.extractUserId(jwt);
                logger.info("Token valid, userId: " + userId);
                
                // 4. Create authentication token and set in SecurityContext
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    Collections.emptyList()
                );
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Authentication set in SecurityContext");
            } else {
                logger.warn("Token validation failed");
            }
        } catch (Exception e) {
            // Token is invalid, continue without authentication
            logger.error("JWT authentication failed", e);
        }

        filterChain.doFilter(request, response);
    }
}
