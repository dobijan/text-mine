package hu.bme.mit.textmine.mongo.note.model;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Language;
import org.springframework.data.mongodb.core.mapping.TextScore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import com.querydsl.core.annotations.QueryEntity;

import hu.bme.mit.textmine.mongo.core.BaseMongoEntity;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@org.springframework.data.mongodb.core.mapping.Document(collection = "notes")
@QueryEntity
@CompoundIndexes({
        @CompoundIndex(name = "doc", def = "{'document.$id' : 1}"),
        @CompoundIndex(name = "doc_section", def = "{'document.$id' : 1, 'section' : 1}"),
        @CompoundIndex(name = "doc_type", def = "{'document.$id' : 1, 'type' : 1}"),
        @CompoundIndex(name = "doc_type_subtype", def = "{'document.$id' : 1, 'type' : 1, 'subType' : 1}"),
        @CompoundIndex(name = "doc_quote", def = "{'document.$id' : 1, 'quote' : 1}")
})
// @Slf4j
public class Note extends BaseMongoEntity {

    private static final long serialVersionUID = -2197819671880549251L;

    // @DBRef(lazy = true)
    @NotNull(message = "Annotation must be linked to a document!")
    private String documentId;

    // public Document getDocument() {
    // log.info("Getting note document!");
    // return document;
    // }

    @Indexed
    @NotNull(message = "Annotation must have a type!")
    private String type;

    @Indexed
    @NotNull(message = "Annotation must have a subtype!")
    private String subType;

    @Indexed
    @TextIndexed(weight = 2)
    @NotNull(message = "Annotation must have a quote!")
    private String quote;

    @NotNull(message = "Annotation must have a line references list!")
    private Set<Integer> lineRefs;

    @NotNull(message = "Annotation must have a section reference!")
    private Integer section;

    @TextIndexed
    @NotNull(message = "Annotation must have content!")
    private String content;

    @TextScore
    @JsonIgnore
    private Double score;

    @Language
    @JsonIgnore
    private final String language = "none";

    @Override
    public String getHash() {
        return Hashing.sha256().hashString(String.join(";", this.documentId,
                this.quote, this.content, this.type, this.subType), StandardCharsets.UTF_8).toString();
    }

    @Override
    public String getResourcePostfix(TextMineVocabularyService vocabulary) {
        return vocabulary.getNoteResourcePostfix();
    }
}
