package hu.bme.mit.textmine.rdf.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import hu.bme.mit.textmine.mongo.core.RdfEntity;
import lombok.Getter;

@Service
public class TextMineVocabularyService {

    @Getter
    @Value("${vocabulary.base.iri}")
    private String baseIri;

    @Value("${vocabulary.base.resource.postfix}")
    private String baseResourcePostfix;

    @Getter
    @Value("${vocabulary.corpus.resource.postfix}")
    private String corpusResourcePostfix;

    @Getter
    @Value("${vocabulary.article.resource.postfix}")
    private String articleResourcePostfix;

    @Getter
    @Value("${vocabulary.document.resource.postfix}")
    private String documentResourcePostfix;

    @Getter
    @Value("${vocabulary.section.resource.postfix}")
    private String sectionResourcePostfix;

    @Getter
    @Value("${vocabulary.line.resource.postfix}")
    private String lineResourcePostfix;

    @Getter
    @Value("${vocabulary.note.resource.postfix}")
    private String noteResourcePostfix;

    @Value("${vocabulary.base.relation.postfix}")
    private String baseRelationPostfix;

    @Value("${vocabulary.location.relation.postfix}")
    private String locationRelationPostfix;

    @Value("${vocabulary.person.relation.postfix}")
    private String personRelationPostfix;

    @Value("${vocabulary.person.relation.postfix}")
    private String temporalRelationPostfix;

    public String document() {
        return String.join("/", this.baseIri, this.baseResourcePostfix, this.documentResourcePostfix);
    }

    public String article() {
        return String.join("/", this.baseIri, this.baseResourcePostfix, this.articleResourcePostfix);
    }

    public String note() {
        return String.join("/", this.baseIri, this.baseResourcePostfix, this.noteResourcePostfix);
    }

    public String corpus() {
        return String.join("/", this.baseIri, this.baseResourcePostfix, this.corpusResourcePostfix);
    }

    public String asResource(RdfEntity entity) {
        return String.join("/", this.baseIri, this.baseResourcePostfix, entity.getResourcePostfix(this),
                entity.getHash());
    }

    public String personRelation() {
        return String.join("/", this.baseIri, this.baseRelationPostfix, this.personRelationPostfix);
    }

    public String locationRelation() {
        return String.join("/", this.baseIri, this.baseRelationPostfix, this.locationRelationPostfix);
    }

    public String temporalRelation() {
        return String.join("/", this.baseIri, this.baseRelationPostfix, this.temporalRelationPostfix);
    }
}
