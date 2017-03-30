package hu.bme.mit.textmine.mongo.document.model;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.DBRef;

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
public class Document extends BaseMongoEntity {

    private static final long serialVersionUID = -7517719859994825094L;

    @DBRef
    @NotNull(message = "Document corpus must not be null!")
    private Corpus corpus;
    
    @NotNull(message = "Document title must not be null!")
    private String title;

    @NotNull(message = "Document content must not be null!")
    private String content;

    @NotNull(message = "Document author must not be null!")
    private String author;

    @NotNull(message = "Document sections must not be null!")
    private List<Section> sections;
    
    @NotNull(message = "Document pages must not be null!")
    private List<Section> pages;
}
