package hu.bme.mit.textmine.mongo.dictionary.dal;

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.dictionary.model.QArticle;

public interface ArticleRepository
        extends MongoRepository<Article, String>, CustomArticleRepository, QueryDslPredicateExecutor<Article> {

    public List<Article> findAll();

    // @Query(value = "{ 'document.$id' : ?0 }")
    public default List<Article> findByDocumentId(ObjectId id) {
        List<Article> result = Lists.newArrayList();
        this.findAll(new QArticle("article").document.id.eq(id)).forEach(result::add);
        return result;
    }

    public List<Article> findByEntryWord(String entryWord);

    public default boolean exists(String id) {
        return this.exists(new QArticle("article").id.eq(new ObjectId(id)));
    }

    public Page<Article> findBy(TextCriteria criteria, Pageable page);

    public List<Article> findByPartOfSpeech(PartOfSpeech pos);

    public List<Article> findAllByOrderByScoreDesc(TextCriteria criteria);

    public default Set<Article> languageAgnosticQuery(List<String> phrases) {
        Set<Article> articles = Sets.newHashSet();
        for (String phrase : phrases) {
            articles.addAll(this.findAllByOrderByScoreDesc(TextCriteria.forLanguage("none").caseSensitive(true)
                    .diacriticSensitive(true).matchingPhrase(phrase)));

        }
        return articles;
    }
}
