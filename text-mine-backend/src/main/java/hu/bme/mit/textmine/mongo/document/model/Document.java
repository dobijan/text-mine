package hu.bme.mit.textmine.mongo.document.model;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Language;
import org.springframework.data.mongodb.core.mapping.TextScore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import com.querydsl.core.annotations.QueryEntity;

import hu.bme.mit.textmine.mongo.core.BaseMongoEntity;
import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
@QueryEntity
@CompoundIndexes({
        @CompoundIndex(name = "corpus", def = "{'corpus.$id' : 1}"),
        @CompoundIndex(name = "corpus_author", def = "{'corpus.$id' : 1, 'author': 1}"),
        @CompoundIndex(name = "corpus_title", def = "{'corpus.$id' : 1, 'title': 1}"),
        @CompoundIndex(name = "author_title", def = "{'author' : 1, 'title': 1}")
})
public class Document extends BaseMongoEntity {

    private static final long serialVersionUID = -7517719859994825094L;

    @DBRef
    @NotNull(message = "Document corpus must not be null!")
    private Corpus corpus;

    @Indexed
    @TextIndexed(weight = 2)
    @NotNull(message = "Document title must not be null!")
    private String title;

    @TextIndexed
    @NotNull(message = "Document content must not be null!")
    private String content;

    @TextIndexed
    private String normalized;

    @Indexed
    @TextIndexed(weight = 2)
    @NotNull(message = "Document author must not be null!")
    private String author;

    @NotNull(message = "Document sections must not be null!")
    private List<Section> sections;

    @NotNull(message = "Document pages must not be null!")
    private List<Section> pages;

    @TextScore
    @JsonIgnore
    private Double score;

    @Language
    @JsonIgnore
    private final String language = "none";

    @Override
    public String getHash() {
        return Hashing.sha256()
                .hashString(String.join(";", this.corpus.getTitle(), this.author, this.title, this.content),
                        StandardCharsets.UTF_8)
                .toString();
    }

    @Override
    public String getResourcePostfix(TextMineVocabularyService vocabulary) {
        return vocabulary.getDocumentResourcePostfix();
    }
}
