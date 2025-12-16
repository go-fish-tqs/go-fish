package gofish.pt.config;

import gofish.pt.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@Profile("!test")
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with custom configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRF protection is disabled because this is a stateless REST API using JWT
                // tokens
                // SonarQube: This is safe for APIs that don't use session-based authentication
                .csrf(csrf -> csrf.disable()) // NOSONAR
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Allow all OPTIONS requests (CORS preflight)
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        // Public endpoints that don't require authentication
                        .requestMatchers("/api/auth/**").permitAll()
                        // User's own items require authentication (must come before public GET)
                        .requestMatchers("/v3/api-docs/*", "/swagger-ui/*", "/swagger-ui.html").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/items/my").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/items/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/items/filter").permitAll() // Filter/search
                                                                                                                    // is
                                                                                                                    // public
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/bookings/item/**").permitAll() // Calendar
                                                                                                                       // availability
                                                                                                                       // is
                                                                                                                       // public
                        .requestMatchers("/actuator/**").permitAll()
                        // Admin-only endpoints - require ROLE_ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Protected endpoints - require authentication
                        .requestMatchers("/api/bookings/**").authenticated()
                        .requestMatchers("/api/reviews/**").authenticated() // POST/PUT/DELETE require auth
                        .requestMatchers("/api/items/**").authenticated() // POST/PUT/DELETE require auth
                        .requestMatchers("/api/payments/**").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        .anyRequest().authenticated())
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
