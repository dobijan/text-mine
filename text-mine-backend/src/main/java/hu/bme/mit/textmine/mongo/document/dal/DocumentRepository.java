package hu.bme.mit.textmine.mongo.document.dal;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.QDocument;

public interface DocumentRepository extends MongoRepository<Document, String>, QueryDslPredicateExecutor<Document> {

    public List<Document> findAll();

    @Query(value = "{ 'corpus.$id' : ?0 }")
    public List<Document> findByCorpusId(ObjectId id);

    public default boolean exists(String id) {
        return this.exists(new QDocument("document").id.eq(new ObjectId(id)));
    }

    public List<Document> findByTitle(String title);

    public Page<Document> findBy(TextCriteria criteria, Pageable page);

    public List<Document> findAllByOrderByScoreDesc(TextCriteria criteria);

    public default List<Document> languageAgnosticQuery(String word) {
        return this.findAllByOrderByScoreDesc(
                TextCriteria.forLanguage("none").caseSensitive(false).diacriticSensitive(false).matching(word));
    }
}
