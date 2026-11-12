package hariom.linkedin.postservice.service;

import hariom.linkedin.postservice.dto.AddComment;
import hariom.linkedin.postservice.dto.CommentResponse;
import hariom.linkedin.postservice.dto.CreatePost;
import hariom.linkedin.postservice.dto.PostResponse;
import hariom.linkedin.postservice.entity.Comment;
import hariom.linkedin.postservice.entity.Like;
import hariom.linkedin.postservice.entity.Post;
import hariom.linkedin.postservice.repository.CommentRepository;
import hariom.linkedin.postservice.repository.LikeRepository;
import hariom.linkedin.postservice.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final S3Service s3Service;

    private final KafkaTemplate<Long, Object> kafkaTemplate;

    private static final String POST_CREATED_TOPIC = "post.created";
    private static final String POST_LIKED_TOPIC = "post.liked";
    private static final String POST_COMMENTED_TOPIC = "post.commented";

    /**
     * Create a new post.
     * Optionally Upload image to S3 and get it url.
     * Publish event to Kafka to be consumed by feed & search service.
     */
    public PostResponse createPost(Long authorId, CreatePost request, MultipartFile file) {
        log.info("Creating post for user: {}", authorId);

        String imageUrl = null;

        if (file != null && !file.isEmpty()) {
            imageUrl = s3Service.uploadFile(file, "posts/" + authorId);
        }

        Post post = Post.builder()
                .authorId(authorId)
                .content(request.getContent())
                .imageUrl(imageUrl)
                .likeCount(0)
                .commentCount(0)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("Post created: {}", savedPost.getId());

        // Publish post.created event to Kafka
        // Feed & Search service consume this event

        Map<String, Object> postCreatedEvent = new HashMap<>();

        postCreatedEvent.put("id", savedPost.getId());
        postCreatedEvent.put("authorId", savedPost.getAuthorId());
        postCreatedEvent.put("content", savedPost.getContent());
        postCreatedEvent.put("imageUrl", savedPost.getImageUrl());
        postCreatedEvent.put("createdAt", savedPost.getCreatedAt().toString());

        kafkaTemplate.send(POST_CREATED_TOPIC, savedPost.getId(), postCreatedEvent);

        log.info("Post created event published: {}", savedPost.getId());

        return null;
    }

    /**
     * Get post details by id.
     */
    public PostResponse getPost(Long postId) {
        Post post = getPostById(postId);
        return mapPostToResponse(post);
    }

    /**
     * Get all posts of a user.
     */
    public List<PostResponse> getAllPost(Long authorId) {
        List<Post> posts = postRepository.findPostByAuthorIdOrderByCreatedAtDesc(authorId);

        return posts.stream()
                .map(this::mapPostToResponse)
                .toList();
    }

    /**
     * Like / Unlike a post.
     * Publish event to Kafka.
     */
    @Transactional
    public void likeOrUnlikePost(Long postId, Long userId) {
        Post post = getPostById(postId);

        boolean alreadyLiked = likeRepository.existsByPostIdAndUserId(postId, userId);

        if (alreadyLiked) {
            // Unlike post
            likeRepository
                    .findByPostIdAndUserId(postId, userId)
                    .ifPresent(likeRepository::delete);

            // Update like count of post
            post.setLikeCount(post.getLikeCount() - 1);

        } else {
            // Like post
            Like like = Like.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();

            likeRepository.save(like);

            // Update like count of post
            post.setLikeCount(post.getLikeCount() + 1);

            // Publish post liked event to Kafka

            Map<String, Object> postLikedEvent = new HashMap<>();
            postLikedEvent.put("id", postId);
            postLikedEvent.put("userId", userId);
            postLikedEvent.put("authorId", post.getAuthorId());

            kafkaTemplate.send(POST_LIKED_TOPIC, postId, postLikedEvent);
        }
    }

    /**
     * Add a comment on post.
     * Publish event to Kafka.
     */
    @Transactional
    public CommentResponse addComment(Long postId, Long authorId, AddComment request) {
        Post post = getPostById(postId);

        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(authorId)
                .content(request.getComment())
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Update comment count of post
        post.setCommentCount(post.getCommentCount() + 1);

        // Publish post.commented event to Kafka

        Map<String, Object> postCommentedEvent = new HashMap<>();
        postCommentedEvent.put("id", postId);
        postCommentedEvent.put("authorId", authorId);
        postCommentedEvent.put("commentId", savedComment.getId());
        postCommentedEvent.put("postAuthorId", post.getAuthorId());

        kafkaTemplate.send(POST_COMMENTED_TOPIC, postId, postCommentedEvent);

        return mapCommentToResponse(savedComment);
    }

    /**
     * Get all comment of a post.
     */
    public List<CommentResponse> getAllComment(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostIdOrderByCreatedAtDesc(postId);
        return comments.stream()
                .map(this::mapCommentToResponse)
                .toList();
    }

    /**
     * Delete a post.
     * User can only delete their own posts.
     */
    public void deletePost(Long postId, Long authorId) {
        Post post = getPostById(postId);

        if (!post.getAuthorId().equals(authorId)) {
            throw new RuntimeException("User can only delete their own posts");
        }

        postRepository.delete(post);
    }

    /**
     * Delete a comment.
     * User can only delete their comments.
     * Post owner can delete comments on its post.
     * Update posts comment count.
     */
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long authorId) {
        Post post = getPostById(postId);

        Comment comment = commentRepository.findByIdAndPostId(commentId, postId).orElseThrow(
                () -> new RuntimeException("Comment not found")
        );

        if (!post.getAuthorId().equals(authorId) && !comment.getAuthorId().equals(authorId)) {
            throw new RuntimeException("User can only delete their own comments");
        }

        commentRepository.delete(comment);
        post.setCommentCount(post.getCommentCount() - 1);
    }

    /**
     * Helper method to get post by id.
     */
    private Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException("Post not found: " + postId)
        );
    }

    /**
     * Helper method to map Post to PostResponse.
     */
    private PostResponse mapPostToResponse(Post post) {
        return PostResponse.builder()
                .authorId(post.getAuthorId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    /**
     * Helper method to map Comment to CommentResponse.
     */
    private CommentResponse mapCommentToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
