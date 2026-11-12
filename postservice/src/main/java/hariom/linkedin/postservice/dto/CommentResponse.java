package hariom.linkedin.postservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class CommentResponse {
    private Long id;
    private Long postId;
    private Long authorId;
    private String content;
    private LocalDateTime updatedAt;
}
