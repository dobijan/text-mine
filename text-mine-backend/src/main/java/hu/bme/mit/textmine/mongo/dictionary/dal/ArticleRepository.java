package hu.bme.mit.textmine.mongo.dictionary.dal;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;

public interface ArticleRepository extends MongoRepository<Article, String>, QueryDslPredicateExecutor<Article> {
    public List<Article> findAll();
    
    @Query(value = "{ 'document.$id' : ?0 }")
    public List<Article> findByDocumentId(ObjectId id);
}
