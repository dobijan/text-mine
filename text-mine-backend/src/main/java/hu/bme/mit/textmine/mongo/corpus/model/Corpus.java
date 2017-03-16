package hu.bme.mit.textmine.mongo.corpus.model;

import org.springframework.data.mongodb.core.mapping.Document;

import hu.bme.mit.textmine.mongo.core.BaseMongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "corpora")
public class Corpus extends BaseMongoEntity {

    private static final long serialVersionUID = -7874690710867901379L;

    private String title;
    
    private String description;
}
