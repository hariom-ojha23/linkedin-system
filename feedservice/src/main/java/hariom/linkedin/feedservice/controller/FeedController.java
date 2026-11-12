package hariom.linkedin.feedservice.controller;

import hariom.linkedin.feedservice.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;


    /**
     * Get paginated feed for a user
     * Returns list of postIds
     * Client fetches full post details from post service
     */
    @GetMapping()
    public ResponseEntity<List<Long>> getFeed(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(feedService.getFeed(userId, page, size));
    }

    /**
     * Clear feed cache - useful for testing
     */
    @GetMapping("/clear")
    public ResponseEntity<String> clearFeedCache(
            @RequestHeader("X-User-Id") Long userId
    ) {
        feedService.clearFeedCache(userId);
        return ResponseEntity.ok("Feed cleared");
    }
}
