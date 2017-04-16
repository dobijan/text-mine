package hu.bme.mit.textmine.rdf.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.note.model.Note;
import lombok.Getter;

@Service
public class TextMineVocabularyService {

    @Getter
    @Value("${vocabulary.base.iri}")
    private String baseIri;

    @Value("${vocabulary.base.resource.postfix}")
    private String baseResourcePostfix;

    @Value("${vocabulary.corpus.resource.postfix}")
    private String corpusResourcePostfix;

    @Value("${vocabulary.article.resource.postfix}")
    private String articleResourcePostfix;

    @Value("${vocabulary.document.resource.postfix}")
    private String documentResourcePostfix;

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

    public String asResource(Document document) {
        return String.join("/", this.baseIri, this.baseResourcePostfix, this.documentResourcePostfix,
                document.getHash());
    }

    public String asResource(Corpus corpus) {
        return String.join("/", this.baseIri, this.baseResourcePostfix, this.corpusResourcePostfix, corpus.getHash());
    }

    public String asResource(Article article) {
        return String.join("/", this.baseIri, this.baseResourcePostfix, this.articleResourcePostfix, article.getHash());
    }

    public String asResource(Note note) {
        return String.join("/", this.baseIri, this.baseResourcePostfix, this.noteResourcePostfix, note.getHash());
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
