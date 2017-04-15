package hu.bme.mit.textmine.mongo.dictionary.model;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.google.common.hash.Hashing;
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
@CompoundIndexes({
        @CompoundIndex(name = "doc", def = "{'document.$id' : 1}"),
        @CompoundIndex(name = "doc_entryWord", def = "{'document.$id' : 1, 'entryWord': 1}"),
        @CompoundIndex(name = "doc_properNoun", def = "{'document.$id' : 1, 'properNoun': 1}"),
        @CompoundIndex(name = "doc_derivative", def = "{'document.$id' : 1, 'derivative': 1}"),
        @CompoundIndex(name = "doc_pos", def = "{'document.$id' : 1, 'partOfSpeech': 1}")
})
public class Article extends BaseMongoEntity {

    private static final long serialVersionUID = -383674994159227800L;

    @DBRef
    @NotNull(message = "Article must be linked to a document!")
    private Document document;

    @NotNull(message = "Article entry word must not be null!")
    @Indexed
    private String entryWord;

    @Indexed
    @NotNull(message = "Article proper noun flag must not be null!")
    private Boolean properNoun;

    @Indexed
    @NotNull(message = "Article derivative flag must not be null!")
    private Boolean derivative;

    private String editorNote;

    private String meaning;

    @Indexed
    private PartOfSpeech partOfSpeech;

    private List<String> externalReferences;

    private List<String> internalReferences;

    private List<FormVariant> formVariants;

    @Override
    public String getHash() {
        return Hashing.sha256()
                .hashString(String.join(";", this.document.getAuthor(), this.document.getTitle(), this.entryWord),
                        StandardCharsets.UTF_8)
                .toString();
    }
}
