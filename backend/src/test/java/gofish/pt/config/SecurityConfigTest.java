package gofish.pt.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void passwordEncoder_isProvided() {
        SecurityConfig cfg = new SecurityConfig();
        assertThat(cfg.passwordEncoder()).isNotNull();
    }
}
