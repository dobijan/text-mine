package hu.bme.mit.textmine.mongo.document.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.DBRef;

import hu.bme.mit.textmine.mongo.core.BaseMongoEntity;
import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
public class Document extends BaseMongoEntity {

    private static final long serialVersionUID = -7517719859994825094L;

    @DBRef
    private Corpus corpus;
    
    private String title;

    private String content;

    private String author;

    private List<Section> sections;
}
