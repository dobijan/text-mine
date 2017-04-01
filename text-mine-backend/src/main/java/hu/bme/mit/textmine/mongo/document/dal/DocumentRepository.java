package hu.bme.mit.textmine.mongo.document.dal;

import java.util.List;

import org.bson.types.ObjectId;
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
}
