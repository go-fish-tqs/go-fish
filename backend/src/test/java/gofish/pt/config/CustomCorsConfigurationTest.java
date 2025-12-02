package gofish.pt.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CustomCorsConfigurationTest {

    private CustomCorsConfiguration customCorsConfiguration;

    @BeforeEach
    void setUp() {
        customCorsConfiguration = new CustomCorsConfiguration();
    }

    @Test
    void getCorsConfiguration_ShouldReturnCorrectConfig() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        // Act
        CorsConfiguration config = customCorsConfiguration.getCorsConfiguration(mockRequest);

        // Assert
        assertNotNull(config, "CorsConfiguration should not be null");

        // Check Allowed Origins
        List<String> expectedOrigins = List.of("http://localhost:3000", "http://127.0.0.1:3000");
        assertEquals(expectedOrigins, config.getAllowedOrigins(), "Allowed origins do not match");

        // Check Allowed Methods
        List<String> expectedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        assertEquals(expectedMethods, config.getAllowedMethods(), "Allowed methods do not match");

        // Check Allowed Headers
        assertEquals(List.of("*"), config.getAllowedHeaders(), "Allowed headers should be '*'");

        // Check Allow Credentials
        assertTrue(config.getAllowCredentials(), "Allow credentials should be true");

        // Check Max Age
        assertEquals(3600L, config.getMaxAge(), "Max age should be 3600 seconds");
    }
}
