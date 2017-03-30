package hu.bme.mit.textmine.mongo.dictionary.model;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.DBRef;

import com.querydsl.core.annotations.QueryEntity;

import hu.bme.mit.textmine.mongo.core.BaseMongoEntity;
import hu.bme.mit.textmine.mongo.document.model.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@org.springframework.data.mongodb.core.mapping.Document(collection = "articles")
@QueryEntity
public class Article extends BaseMongoEntity {

    private static final long serialVersionUID = -383674994159227800L;
    
    @DBRef
    @NotNull(message = "Article must be linked to a document!")
    private Document document;

    @NotNull(message = "Article entry word must not be null!")
    private String entryWord;

    @NotNull(message = "Article proper noun flag must not be null!")
    private Boolean properNoun;

    @NotNull(message = "Article derivative flag must not be null!")
    private Boolean derivative;
    
    private String editorNote;
    
    private String meaning;

    private List<String> externalReferences;
    
    private List<String> internalReferences;
    
    private List<FormVariant> formVariants;
}
