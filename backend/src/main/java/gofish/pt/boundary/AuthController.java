package gofish.pt.boundary;

import gofish.pt.dto.UserRegistrationRequest;
import gofish.pt.entity.User;
import gofish.pt.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegistrationRequest req) {
        User saved = authService.register(req);
        URI location = URI.create("/api/users/" + saved.getId());
        return ResponseEntity.created(location).body(Map.of("userId", saved.getId()));
    }
}
