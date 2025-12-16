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

        // ✅ UPDATE: Add your VM domain here
        configuration.setAllowedOrigins(Arrays.asList(
                // Development
                // Dev (Direct access)
                "http://localhost:3000",

                // ✅ TEST FIX: Allow Nginx HTTPS access locally
                "https://localhost",
                "https://127.0.0.1",
                "http://localhost",
                "http://127.0.0.1",

                // Production VM (Keep this for later)
                "https://deti-tqs-03.ua.pt",   // <-- YOUR VM (HTTP fallback)
                "http://deti-tqs-03.ua.pt"   // <-- YOUR VM (HTTP fallback)
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Location"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}