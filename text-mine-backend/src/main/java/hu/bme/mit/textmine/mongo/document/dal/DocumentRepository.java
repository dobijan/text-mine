package hu.bme.mit.textmine.mongo.document.dal;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import hu.bme.mit.textmine.mongo.document.model.Document;

public interface DocumentRepository extends MongoRepository<Document, String> {
    public List<Document> findAll();
    
    @Query(value = "{ 'corpus.$id' : ?0 }")
    public List<Document> findByCorpusId(ObjectId id);
}
