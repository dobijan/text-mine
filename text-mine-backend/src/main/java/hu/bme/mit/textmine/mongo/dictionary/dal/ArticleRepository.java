package hu.bme.mit.textmine.mongo.dictionary.dal;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.QArticle;

public interface ArticleRepository
        extends MongoRepository<Article, String>, CustomArticleRepository, QueryDslPredicateExecutor<Article> {

    public List<Article> findAll();

    @Query(value = "{ 'document.$id' : ?0 }")
    public List<Article> findByDocumentId(ObjectId id);

    public List<Article> findByEntryWord(String entryWord);

    public default boolean exists(String id) {
        return this.exists(new QArticle("article").id.eq(new ObjectId(id)));
    }

    public Page<Article> findBy(TextCriteria criteria, Pageable page);

    public List<Article> findAllByOrderByScoreDesc(TextCriteria criteria);

    public default List<Article> languageAgnosticQuery(String word) {
        return this.findAllByOrderByScoreDesc(
                TextCriteria.forLanguage("none").caseSensitive(false).diacriticSensitive(false).matching(word));
    }
}
