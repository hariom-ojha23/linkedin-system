package hariom.linkedin.searchservice.repository;

import hariom.linkedin.searchservice.model.PostDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {

    @Query(
            "{\"match\": { \"content\": { \"query\": \"?0\", \"fuzziness\": \"auto\" } } }"
    )
    List<PostDocument> searchPosts(String query);

}
