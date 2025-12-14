package gofish.pt.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection disabled for integration tests - this is acceptable in test environment
            .csrf(csrf -> csrf.disable()) // NOSONAR
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // NOSONAR - All requests allowed in test context
            );
        return http.build();
    }
}
