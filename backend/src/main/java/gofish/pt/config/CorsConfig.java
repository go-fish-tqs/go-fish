package gofish.pt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Combine Dev (localhost) and Prod (deti-tqs-03) domains here
        configuration.setAllowedOrigins(Arrays.asList(
                // Development
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173",

                // Production (Your VM)
                // Note: Add both HTTPS (primary) and HTTP (just in case)
                "https://deti-tqs-03.ua.pt",
                "http://deti-tqs-03.ua.pt"
        ));

        // Allow all standard HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies/auth headers)
        configuration.setAllowCredentials(true);

        // Expose headers needed by the frontend
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Location"));

        // Cache the preflight check for 1 hour to reduce traffic
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}