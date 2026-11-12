package hariom.linkedin.postservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddComment {
    @NotBlank(message = "Comment cannot be blank")
    private String comment;
}
