package hu.bme.mit.textmine.mongo.corpus.model;

import java.nio.charset.StandardCharsets;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Language;
import org.springframework.data.mongodb.core.mapping.TextScore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import com.querydsl.core.annotations.QueryEntity;

import hu.bme.mit.textmine.mongo.core.BaseMongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "corpora")
@QueryEntity
public class Corpus extends BaseMongoEntity {

    private static final long serialVersionUID = -7874690710867901379L;

    @Indexed
    @TextIndexed(weight = 2)
    @NotNull(message = "Corpus title must not be null!")
    private String title;

    @TextIndexed
    @NotNull(message = "Corpus description must not be null!")
    private String description;

    @TextScore
    @JsonIgnore
    private Double score;

    @Language
    @JsonIgnore
    private final String language = "none";

    @Override
    public String getHash() {
        return Hashing.sha256().hashString(String.join(";", this.title, this.description), StandardCharsets.UTF_8)
                .toString();
    }
}
