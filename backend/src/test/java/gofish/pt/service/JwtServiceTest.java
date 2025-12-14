package gofish.pt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);
    }

    @Test
    @DisplayName("Should generate valid JWT token with userId and email")
    void testGenerateToken() {
        // Arrange
        Long userId = 123L;
        String email = "test@example.com";

        // Act
        String token = jwtService.generateToken(userId, email);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Should extract email from valid token")
    void testExtractEmail() {
        // Arrange
        Long userId = 123L;
        String email = "test@example.com";
        String token = jwtService.generateToken(userId, email);

        // Act
        String extractedEmail = jwtService.extractEmail(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should extract userId from valid token")
    void testExtractUserId() {
        // Arrange
        Long userId = 123L;
        String email = "test@example.com";
        String token = jwtService.generateToken(userId, email);

        // Act
        Long extractedUserId = jwtService.extractUserId(token);

        // Assert
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Should validate valid token")
    void testValidateValidToken() {
        // Arrange
        Long userId = 123L;
        String email = "test@example.com";
        String token = jwtService.generateToken(userId, email);

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalid token")
    void testValidateInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject malformed token")
    void testValidateMalformedToken() {
        // Arrange
        String malformedToken = "not-a-jwt-token";

        // Act
        boolean isValid = jwtService.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject empty token")
    void testValidateEmptyToken() {
        // Arrange
        String emptyToken = "";

        // Act
        boolean isValid = jwtService.validateToken(emptyToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject null token")
    void testValidateNullToken() {
        // Arrange
        String nullToken = null;

        // Act
        boolean isValid = jwtService.validateToken(nullToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateDifferentTokensForDifferentUsers() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        // Act
        String token1 = jwtService.generateToken(userId1, email1);
        String token2 = jwtService.generateToken(userId2, email2);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should include userId in token claims")
    void testTokenContainsUserId() {
        // Arrange
        Long userId = 456L;
        String email = "user@example.com";

        // Act
        String token = jwtService.generateToken(userId, email);
        Long extractedUserId = jwtService.extractUserId(token);

        // Assert
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Should include email as subject in token")
    void testTokenContainsEmailAsSubject() {
        // Arrange
        Long userId = 789L;
        String email = "subject@example.com";

        // Act
        String token = jwtService.generateToken(userId, email);
        String extractedEmail = jwtService.extractEmail(token);

        // Assert
        assertEquals(email, extractedEmail);
    }
}
