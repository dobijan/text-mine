package hu.bme.mit.textmine.mongo.corpus.model;

import java.nio.charset.StandardCharsets;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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

    @NotNull(message = "Corpus title must not be null!")
    @Indexed
    private String title;

    @NotNull(message = "Corpus description must not be null!")
    private String description;

    @Override
    public String getHash() {
        return Hashing.sha256().hashString(String.join(";", this.title, this.description), StandardCharsets.UTF_8)
                .toString();
    }
}
