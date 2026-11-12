package hariom.linkedin.postservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePost {
    @NotBlank(message = "Post content cannot be blank")
    private String content;
}
