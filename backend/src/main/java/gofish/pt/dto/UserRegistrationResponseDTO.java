package gofish.pt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponseDTO {
    private Long userId;
    private String message;

    public UserRegistrationResponseDTO(Long userId) {
        this.userId = userId;
        this.message = "User created successfully";
    }
}
