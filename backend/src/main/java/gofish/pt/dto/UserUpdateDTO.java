package gofish.pt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    
    @Size(min = 2, max = 32, message = "Username must be between 2 and 32 characters")
    private String username;
    
    @Email(message = "Email must be valid")
    @Size(max = 64, message = "Email must not exceed 64 characters")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{9,15}$", message = "Phone number must be valid (9-15 digits, optional +)")
    private String phone;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    private String profilePhoto;
    
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;
}
