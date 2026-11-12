package hariom.linkedin.postservice.repository;

import hariom.linkedin.postservice.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findPostByAuthorIdOrderByCreatedAtDesc(Long authorId);

}
