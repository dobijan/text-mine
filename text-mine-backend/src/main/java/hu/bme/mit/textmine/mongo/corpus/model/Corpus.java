package hu.bme.mit.textmine.mongo.corpus.model;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Document;

import hu.bme.mit.textmine.mongo.core.BaseMongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "corpora")
public class Corpus extends BaseMongoEntity {

    private static final long serialVersionUID = -7874690710867901379L;

    @NotNull(message = "Corpus title must not be null!")
    private String title;
    
    @NotNull(message = "Corpus description must not be null!")
    private String description;
}
