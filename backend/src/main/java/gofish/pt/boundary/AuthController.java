package gofish.pt.boundary;

import gofish.pt.dto.UserRegistrationDTO;
import gofish.pt.dto.UserRegistrationResponseDTO;
import gofish.pt.entity.User;
import gofish.pt.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDTO> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        User user = userService.registerUser(registrationDTO);
        UserRegistrationResponseDTO response = new UserRegistrationResponseDTO(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
