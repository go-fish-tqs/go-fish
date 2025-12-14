package gofish.pt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for tests.
 * Disables all security to allow tests to run without authentication.
 */
@Configuration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // NOSONAR - Test environment, security not required
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // NOSONAR - Test environment, all endpoints accessible
            );
        
        return http.build();
    }
}
