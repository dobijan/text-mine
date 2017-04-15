package hu.bme.mit.textmine.mongo.corpus.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.bme.mit.textmine.mongo.corpus.dal.CorpusRepository;
import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.rdf.TextMineVocabularyService;

@Service
public class CorpusService {

    @Autowired
    private CorpusRepository repository;
    
    @Autowired
    private TextMineVocabularyService vocabulary;

    public List<Corpus> getAllCorpora() {
        return this.repository.findAll();
    }

    public Corpus getCorpus(String id) {
        return this.repository.findOne(id);
    }

    public Corpus createCorpus(Corpus corpus) {
        if (Stream.of(corpus.getDescription(), corpus.getTitle()).allMatch(Objects::nonNull)) {
            corpus.setIri(this.vocabulary.asResource(corpus));
            return this.repository.insert(corpus);
        }
        return null;
    }

    public Corpus updateCorpus(Corpus corpus) {
        Corpus oldCorpus = this.repository.findOne(corpus.getId().toString());
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
