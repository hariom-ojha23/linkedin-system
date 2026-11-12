package hariom.linkedin.searchservice.service;

import hariom.linkedin.searchservice.model.PostDocument;
import hariom.linkedin.searchservice.model.UserDocument;
import hariom.linkedin.searchservice.repository.PostSearchRepository;
import hariom.linkedin.searchservice.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchEventConsumer {

    private final UserSearchRepository userSearchRepository;
    private final ObjectMapper objectMapper;
    private final PostSearchRepository postSearchRepository;

    /**
     * Consume user created event and index user.
     */
    @KafkaListener(topics = "user.created")
    public void consumeUserCreated(
            @Payload Map<String, Object> payload
    ) {
        try {
            UserDocument document = objectMapper.convertValue(
                    payload,
                    UserDocument.class
            );

            Long userId = document.getId();

            log.info("Indexing new user: {}", userId);

            userSearchRepository.save(document);

            log.info("User indexed: {}", userId);

        } catch (Exception e) {
            log.error("Error indexing user: {}", e.getMessage());
        }
    }

    /**
     * Consume user updated event and index user
     */
    @KafkaListener(topics = "user.updated")
    public void consumeUserUpdated(
            @Payload Map<String, Object> payload
    ) {
        try {
            UserDocument document = objectMapper.convertValue(
                    payload,
                    UserDocument.class
            );

            Long userId = document.getId();

            log.info("Updating user index: {}", userId);

            userSearchRepository.findById(userId).ifPresent(doc -> {
                doc.setFirstName(document.getFirstName());
                doc.setLastName(document.getLastName());
                doc.setEmail(document.getEmail());
                doc.setHeadLine(document.getHeadLine());
                doc.setLocation(document.getLocation());
                doc.setProfilePhotoUrl(document.getProfilePhotoUrl());

                if (document.getSkills() != null) {
                    doc.setSkills(document.getSkills());
                }

                userSearchRepository.save(doc);

                log.info("User index updated: {}", userId);
            });

        } catch (Exception e) {
            log.error("Error updating user index: {}", e.getMessage());
        }
    }

    /**
     * Consume post created event.
     */
    @KafkaListener(topics = "post.created")
    public void consumePostCreatedEvent(
            @Payload Map<String, Object> payload
    ) {
        try {
            PostDocument document = objectMapper.convertValue(
                    payload,
                    PostDocument.class
            );

            Long postId = document.getId();

            log.info("Indexing new post: {}", postId);

            postSearchRepository.save(document);

            log.info("Post indexed: {}", postId);

        } catch (Exception e) {
            log.error("Error indexing post: {}", e.getMessage());
        }
    }
}
