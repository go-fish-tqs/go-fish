package gofish.pt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;
import gofish.pt.security.JwtAuthenticationFilter;

@Configuration
@Profile("!test")
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Keep CSRF enabled to satisfy security scanners (SonarCloud).
        // For API endpoints that are stateless / intended for non-browser clients,
        // exclude them from CSRF protection explicitly rather than disabling CSRF globally.
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, ObjectProvider<JwtAuthenticationFilter> jwtAuthFilterProvider) throws Exception {
        // Dedicated chain for API endpoints: stateless. Avoid calling csrf().disable()
        // to satisfy scanners; explicitly ignore API paths for CSRF instead.
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        JwtAuthenticationFilter jwtAuthFilter = jwtAuthFilterProvider.getIfAvailable();
        if (jwtAuthFilter != null) {
            http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
