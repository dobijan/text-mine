package hu.bme.mit.textmine.mongo.corpus.dal;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;

public interface CorpusRepository extends MongoRepository<Corpus, String>, QueryDslPredicateExecutor<Corpus> {
    public List<Corpus> findAll();
}
