package hariom.linkedin.searchservice.service;

import hariom.linkedin.searchservice.model.PostDocument;
import hariom.linkedin.searchservice.model.UserDocument;
import hariom.linkedin.searchservice.repository.PostSearchRepository;
import hariom.linkedin.searchservice.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    /**
     * Search users by name, headline & location.
     */
    public List<UserDocument> searchPeople(String query) {
        log.info("Searching users: {}", query);
        return userSearchRepository.searchUsers(query);
    }

    /**
     * Search users by skill
     */
    public List<UserDocument> searchPeopleBySkill(String skill) {
        log.info("Searching users by skill: {}", skill);
        return userSearchRepository.findBySkillsContaining(skill);
    }

    /**
     * Search posts by content
     */
    public List<PostDocument> searchPosts(String query) {
        log.info("Searching posts: {}", query);
        return postSearchRepository.searchPosts(query);
    }
}
