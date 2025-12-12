package gofish.pt.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void generateAndValidateToken() {
        JwtService svc = new JwtService("testsecrettestsecrettestsecrettestsecret");
        // set expiration
        ReflectionTestUtils.setField(svc, "jwtExpirationMs", 3600000L);

        String token = svc.generateToken(Map.of(), "alice");
        assertThat(token).isNotNull();

        assertThat(svc.extractUsername(token)).isEqualTo("alice");
        assertThat(svc.isValid(token)).isTrue();
    }

    @Test
    void invalidTokenReturnsFalse() {
        JwtService svc = new JwtService("anothersecretanothersecretanothersecret");
        ReflectionTestUtils.setField(svc, "jwtExpirationMs", 3600000L);

        String bad = "not-a-token";
        assertThat(svc.isValid(bad)).isFalse();
        assertThat(svc.extractUsername(bad)).isNull();
    }
}
