package hariom.linkedin.searchservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Document for post search.
 * Indexed when post created.
 * Enable full text search across post content.
 */

@Document(indexName = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String Content;

    @Field(type = FieldType.Keyword)
    private Long authorId;

    private String imageUrl;

    private String createdAt;
}
