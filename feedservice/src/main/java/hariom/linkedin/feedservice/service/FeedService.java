package hariom.linkedin.feedservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String FEED_KEY_PREFIX = "feed";

    /**
     * Get paginated feed for a user
     */
    public List<Long> getFeed(Long userId, int page, int size) {
        log.info("Getting feed for user: {}", userId);

        String feedKey = FEED_KEY_PREFIX + userId;

        // Pagination post range

        // page start from 0
        int start = page * size;
        int end = start + size - 1;

        List<Object> postIds = redisTemplate.opsForList()
                .range(feedKey, start, end);

        if (postIds == null || postIds.isEmpty()) {
            log.info("Feed is empty for user: {}", userId);
            return new ArrayList<>();
        }

        List<Long> result = postIds.stream()
                .map(id -> (Long) id)
                .toList();

        log.info("Return {} post for user {}", result.size(), userId);

        return result;
    }

    /**
     * Clear feed cache for a user
     */
    public void clearFeedCache(Long userId) {
        String feedKey = FEED_KEY_PREFIX + userId;
        redisTemplate.delete(feedKey);

        log.info("Feed cache cleared for user: {}", userId);
    }
}
