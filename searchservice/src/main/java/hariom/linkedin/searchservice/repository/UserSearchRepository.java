package hariom.linkedin.searchservice.repository;

import hariom.linkedin.searchservice.model.UserDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, Long> {

    /**
     * Find users using firstName, lastName, headLine or location
     */
    @Query(
                    "{\"multi_match\": {\"query\": \"?0\", " +
                    "\"fields\": [\"firstName\", \"lastName\", \"headLine\", \"location\"]}}"
    )
    List<UserDocument> searchUsers(String query);

    /**
     * Search by specific skill
     */
    List<UserDocument> findBySkillsContaining(String skill);
}
