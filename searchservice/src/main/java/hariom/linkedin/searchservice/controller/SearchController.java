package hariom.linkedin.searchservice.controller;

import hariom.linkedin.searchservice.model.PostDocument;
import hariom.linkedin.searchservice.model.UserDocument;
import hariom.linkedin.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Search people by name, location, headline
     */
    @GetMapping("/people")
    public ResponseEntity<List<UserDocument>> searchPeople(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(searchService.searchPeople(query));
    }

    /**
     * Search people by skills
     */
    @GetMapping("/people/skills")
    public ResponseEntity<List<UserDocument>> searchBySkill(
            @RequestParam String skill
    ) {
        return ResponseEntity.ok(searchService.searchPeopleBySkill(skill));
    }

    /**
     * Search by content
     */
    @GetMapping("/posts")
    public ResponseEntity<List<PostDocument>> searchPosts(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(searchService.searchPosts(query));
    }
}
