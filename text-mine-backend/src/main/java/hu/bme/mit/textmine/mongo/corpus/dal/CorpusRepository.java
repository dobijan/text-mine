package hu.bme.mit.textmine.mongo.corpus.dal;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;

public interface CorpusRepository extends MongoRepository<Corpus, String> {
    public List<Corpus> findAll();
}
