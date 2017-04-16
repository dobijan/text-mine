package hu.bme.mit.textmine.mongo.corpus.dal;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;

public interface CorpusRepository extends MongoRepository<Corpus, String>, QueryDslPredicateExecutor<Corpus> {

    public List<Corpus> findAll();

    public Page<Corpus> findBy(TextCriteria criteria, Pageable page);

    public List<Corpus> findAllByOrderByScoreDesc(TextCriteria criteria);

    public default List<Corpus> languageAgnosticQuery(String word) {
        return this.findAllByOrderByScoreDesc(
                TextCriteria.forLanguage("none").caseSensitive(false).diacriticSensitive(false).matching(word));
    }
}
