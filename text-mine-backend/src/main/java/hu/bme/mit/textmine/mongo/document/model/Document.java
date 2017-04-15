package hu.bme.mit.textmine.mongo.document.model;

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
import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
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

    @NotNull(message = "Document title must not be null!")
    private String title;

    @NotNull(message = "Document content must not be null!")
    private String content;

    @Indexed
    @NotNull(message = "Document author must not be null!")
    private String author;

    @NotNull(message = "Document sections must not be null!")
    private List<Section> sections;

    @NotNull(message = "Document pages must not be null!")
    private List<Section> pages;

    @Override
    public String getHash() {
        return Hashing.sha256()
                .hashString(String.join(";", this.corpus.getTitle(), this.author, this.title, this.content),
                        StandardCharsets.UTF_8)
                .toString();
    }
}
