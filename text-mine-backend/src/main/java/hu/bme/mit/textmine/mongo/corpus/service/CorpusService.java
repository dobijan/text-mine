package hu.bme.mit.textmine.mongo.corpus.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import hu.bme.mit.textmine.mongo.corpus.dal.CorpusRepository;
import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;

@Service
public class CorpusService {

    @Autowired
    @Lazy
    private CorpusRepository repository;

    @Autowired
    private TextMineVocabularyService vocabulary;

    public List<Corpus> getAllCorpora() {
        return this.repository.findAll();
    }

    public Corpus getCorpus(String id) {
        return this.repository.findById(id).orElse(null);
    }

    public Set<Corpus> languageAgnosticFullTextQuery(List<String> phrases) {
        return this.repository.languageAgnosticQuery(phrases);
    }

    public Corpus createCorpus(Corpus corpus) {
        if (Stream.of(corpus.getDescription(), corpus.getTitle()).allMatch(Objects::nonNull)) {
            corpus.setIri(this.vocabulary.asResource(corpus));
            return this.repository.insert(corpus);
        }
        return null;
    }

    public Corpus updateCorpus(Corpus corpus) {
        Corpus oldCorpus = this.repository.findById(corpus.getId().toString()).orElse(null);
        if (oldCorpus != null) {
            oldCorpus.setDescription(corpus.getDescription());
            oldCorpus.setTitle(corpus.getTitle());
            return this.repository.save(oldCorpus);
        }
        return null;
    }

    public void removeCorpus(Corpus corpus) {
        this.repository.delete(corpus);
    }
}
