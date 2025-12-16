package gofish.pt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("!dev")  // Only use this in prod/test profiles, not dev (DevWebConfig handles dev)
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Production domains - be explicit, do NOT use "*" when credentials are enabled
        configuration.setAllowedOrigins(Arrays.asList(
                "https://deti-tqs-03.ua.pt",
                "http://deti-tqs-03.ua.pt"
        ));

        // Allow all standard HTTP methods (including OPTIONS for preflight)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Explicitly list allowed headers instead of wildcard
        configuration.setAllowedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Cache-Control",
                "Accept-Language"
        ));

        // Allow credentials (cookies/auth headers) - REQUIRED for login to work
        configuration.setAllowCredentials(true);

        // Expose headers needed by the frontend
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Location", "Set-Cookie"));

        // Cache the preflight check for 1 hour to reduce traffic
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}