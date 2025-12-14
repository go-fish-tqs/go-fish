package gofish.pt.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

public class TestSecurityContextHelper {

    private TestSecurityContextHelper() {
        // Utility class
    }

    /**
     * Set authenticated user in SecurityContext for testing
     * @param userId the user ID to set as authenticated
     */
    public static void setAuthenticatedUser(Long userId) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            userId,
            null,
            Collections.emptyList()
        );
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    /**
     * Clear the SecurityContext
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
