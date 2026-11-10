package hariom.linkedin.userservice.dto;

import hariom.linkedin.userservice.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;
    private String headLine;
    private String about;
    private String location;
    private String profilePhotoUrl;
    private String coverPhotoUrl;
    private List<String> skills;
}
