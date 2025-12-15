package gofish.pt.security;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Requirement("REQ-SEC-001")
    void getAuthenticatedUserId_whenPrincipalIsLong_returnsUserId() {
        // Arrange
        Long expectedUserId = 123L;
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(expectedUserId);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        Long actualUserId = SecurityUtils.getAuthenticatedUserId();

        // Assert
        assertEquals(expectedUserId, actualUserId);
    }

    @Test
    @Requirement("REQ-SEC-002")
    void getAuthenticatedUserId_whenAuthenticationIsNull_throwsException() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            SecurityUtils::getAuthenticatedUserId
        );
        assertEquals("User not authenticated", exception.getMessage());
    }

    @Test
    @Requirement("REQ-SEC-003")
    void getAuthenticatedUserId_whenNotAuthenticated_throwsException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            SecurityUtils::getAuthenticatedUserId
        );
        assertEquals("User not authenticated", exception.getMessage());
    }

    @Test
    @Requirement("REQ-SEC-004")
    void getAuthenticatedUserId_whenPrincipalIsNotLong_throwsException() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("not a long");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            SecurityUtils::getAuthenticatedUserId
        );
        assertEquals("Invalid authentication principal", exception.getMessage());
    }
}
