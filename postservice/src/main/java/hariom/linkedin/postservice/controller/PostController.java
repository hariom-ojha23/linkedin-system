package hariom.linkedin.postservice.controller;

import hariom.linkedin.postservice.dto.AddComment;
import hariom.linkedin.postservice.dto.CommentResponse;
import hariom.linkedin.postservice.dto.CreatePost;
import hariom.linkedin.postservice.dto.PostResponse;
import hariom.linkedin.postservice.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@Slf4j
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Create a new post
     */
    @PostMapping()
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePost request,
            @RequestParam(required = false) MultipartFile file,
            @RequestHeader("X-User-Id") Long authorId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.createPost(authorId, request, file));
    }

    /**
     * Get post details
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    /**
     * Get all post of a user
     */
    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<List<PostResponse>> getAllPost(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(postService.getAllPost(userId));
    }

    /**
     * Like a post
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likeOrUnlikePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        postService.likeOrUnlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a comment on a post
     */
    @PostMapping("/{postId}/comment")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long authorId,
            @Valid @RequestBody AddComment request
            ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postService.addComment(postId, authorId, request));
    }

    /**
     * Get all comments of a post
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getAllComment(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(postService.getAllComment(postId));
    }

    /**
     * Delete a post
     * User can only delete own posts
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long authorId
    ) {
        postService.deletePost(postId, authorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a comment
     * User can only delete own comments
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestHeader("X-User-Id") Long authorId
    ) {
        postService.deleteComment(postId, commentId, authorId);
        return ResponseEntity.noContent().build();
    }
}
