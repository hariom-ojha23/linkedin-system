package hariom.linkedin.feedservice.service;

import hariom.linkedin.feedservice.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedEventConsumer {

    private final UserServiceClient userServiceClient;
    private static final String FEED_KEY_PREFIX = "feed";
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${feed.max-size}")
    private int maxFeedSize;

    /**
     * Consume post create payload from Kafka.
     * When a user create a post, immediately push that post
     * to all their connections feed.
     * This is how LinkedIn/Twitter generates feed
     */
    @KafkaListener(topics = "post.created")
    public void consumePostCreated(@Payload Map<String, Object> payload) {
        try {
            Long postId = (Long) payload.get("postId");
            Long authorId = (Long) payload.get("authorId");

            // Get all connections of the post author
            List<Map<String, Object>> connections = userServiceClient
                    .getAllConnection(authorId);

            // Push pot to each connection's feed
            for (Map<String, Object> connection : connections) {
                Long connectionId = (Long) connection.get("id");
                String feedKey = FEED_KEY_PREFIX + connectionId;

                // Add post to feed (sorted set - score = timestamp)
                redisTemplate.opsForList().leftPush(feedKey, postId);

                redisTemplate.opsForList()
                        .trim(feedKey, 0, maxFeedSize - 1);

                log.info("Post {} pushed to feed of user {}", postId, connectionId);
            }

            // Push post to author's own feed
            String authorFeedKey = FEED_KEY_PREFIX + authorId;
            redisTemplate.opsForList().leftPush(authorFeedKey, postId);
            redisTemplate.opsForList()
                    .trim(authorFeedKey, 0, maxFeedSize - 1);

        } catch (Exception e) {
            log.error("Error in pushing post to connection feed: {}",  e.getMessage());
        }
    }
}
