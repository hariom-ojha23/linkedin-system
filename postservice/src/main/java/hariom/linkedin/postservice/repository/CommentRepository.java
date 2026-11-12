package hariom.linkedin.postservice.repository;

import hariom.linkedin.postservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndPostId(Long commentId, Long postId);

    List<Comment> findAllByPostIdOrderByCreatedAtDesc(Long postId);
}
