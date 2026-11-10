package hariom.linkedin.userservice.dto;

import hariom.linkedin.userservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String headLine;
    private String about;
    private String location;
    private String profilePhotoUrl;
    private String coverPhotoUrl;
    private UserRole role;
    private List<String> skills;
    private LocalDateTime createdAt;
}
