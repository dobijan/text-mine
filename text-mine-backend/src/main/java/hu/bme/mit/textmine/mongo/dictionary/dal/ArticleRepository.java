package hu.bme.mit.textmine.mongo.dictionary.dal;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.dictionary.model.QArticle;
import hu.bme.mit.textmine.mongo.document.model.Section;

public interface ArticleRepository
        extends MongoRepository<Article, String>, CustomArticleRepository, QueryDslPredicateExecutor<Article> {

    public List<Article> findAll();

    public default List<Article> findByDocumentId(ObjectId id) {
        List<Article> result = Lists.newArrayList();
        this.findAll(new QArticle("article").document.id.eq(id)).forEach(result::add);
        return result;
    }

    public default List<Article> findByFormVariant(String formVariant) {
        List<Article> result = Lists.newArrayList();
        this.findAll(new QArticle("article").formVariants.any().name.eq(formVariant)).forEach(result::add);
        return result;
    }

    public default List<Article> findByInflection(String inflection) {
        List<Article> result = Lists.newArrayList();
        this.findAll(new QArticle("article").formVariants.any().inflections.any().name.eq(inflection))
                .forEach(result::add);
        return result;
    }

    public default List<Article> findwithParams(String entryWord, String formVariant, String inflection,
            PartOfSpeech partOfSpeech, List<String> documentIds, String corpusId, MatchingStrategy matchingStrategy,
            Integer offset, Integer limit) {
        List<Predicate> predicates = Lists.newArrayList();
        List<Predicate> stringPredicates = Lists.newArrayList(null, null, null);
        List<String> stringValues = Lists.newArrayList(entryWord, formVariant, inflection);
        List<StringPath> stringPaths = Lists.newArrayList(QArticle.article.entryWord,
                QArticle.article.formVariants.any().name, QArticle.article.formVariants.any().inflections.any().name);
        BiFunction<String, StringPath, BooleanExpression> matchingFunction = (value, path) -> {
            return MatchingStrategy.STARTS_WITH.equals(matchingStrategy) ? path.startsWith(value)
                    : MatchingStrategy.ENDS_WITH.equals(matchingStrategy) ? path.endsWith(value)
                            : MatchingStrategy.CONTAINS.equals(matchingStrategy) ? path.contains(value)
                                    : path.eq(value);
        };
        for (int i = 0; i < stringPredicates.size(); ++i) {
            if (stringValues.get(i) != null) {
                stringPredicates.set(i, matchingFunction.apply(stringValues.get(i), stringPaths.get(i)));
            }
        }
        predicates.add(documentIds == null || documentIds.isEmpty() ? null
                : QArticle.article.document.id
                        .in(documentIds.stream().map(ObjectId::new).collect(Collectors.toList())));
        predicates.add(entryWord == null ? null : stringPredicates.get(0));
        predicates.add(formVariant == null ? null : stringPredicates.get(1));
        predicates.add(inflection == null ? null : stringPredicates.get(2));
        predicates.add(partOfSpeech == null ? null : QArticle.article.partOfSpeech.contains(partOfSpeech));
        predicates.add(corpusId == null ? null : QArticle.article.document.corpus.id.eq(new ObjectId(corpusId)));
        if (predicates.stream().allMatch(Objects::isNull)) {
            return this.findAll();
        }
        return this.findAll(ExpressionUtils.allOf(predicates),
                new PageRequest(offset == null ? 0 : offset, limit == null ? 100 : limit)).getContent();
    }

    public List<Article> findByEntryWord(String entryWord);

    public default boolean exists(String id) {
        return this.exists(new QArticle("article").id.eq(new ObjectId(id)));
    }

    public Page<Article> findBy(TextCriteria criteria, Pageable page);

    public default List<Article> findByPartOfSpeech(PartOfSpeech pos) {
        List<Article> articles = Lists.newArrayList();
        this.findAll(QArticle.article.partOfSpeech.contains(pos)).forEach(articles::add);
        return articles;
    }

    public List<Article> findAllByOrderByScoreDesc(TextCriteria criteria);

    public default Set<Article> languageAgnosticQuery(List<String> phrases) {
        Set<Article> articles = Sets.newHashSet();
        for (String phrase : phrases) {
            articles.addAll(this.findAllByOrderByScoreDesc(TextCriteria.forLanguage("none").caseSensitive(true)
                    .diacriticSensitive(true).matchingPhrase(phrase)));
        }
        return articles;
    }

    public default List<Article> languageAgnosticMultiWordQuery(List<String> words) {
        List<Article> articles = Lists.newArrayList();
        articles.addAll(this.findAllByOrderByScoreDesc(TextCriteria.forLanguage("none").caseSensitive(false)
                .diacriticSensitive(true).matchingAny(words.toArray(new String[words.size()]))));
        return articles;
    }

    public default List<Article> findByDocumentSection(String documentId, Integer sectionNumber) {
        Section section = this.getDocumentSection(documentId, sectionNumber);
        if (section == null) {
            return Lists.newArrayList();
        }
        List<String> tokens = Lists.newArrayList(section.getContent().split("\\s+")).stream().map(String::toLowerCase)
                .distinct().collect(Collectors.toList());
        return this.languageAgnosticMultiWordQuery(tokens);
    }

    public default List<Article> findByDocumentPage(String documentId, Integer pageNumber) {
        Section page = this.getDocumentPage(documentId, pageNumber);
        if (page == null) {
            return Lists.newArrayList();
        }
        List<String> tokens = Lists.newArrayList(page.getContent().split("\\s+")).stream().map(String::toLowerCase)
                .distinct().collect(Collectors.toList());
        return this.languageAgnosticMultiWordQuery(tokens);
    }

    public default List<Article> findAllByPartsOfSpeech(List<PartOfSpeech> pos) {
        List<Article> articles = Lists.newArrayList();
        this.findAll(QArticle.article.partOfSpeech.any().in(pos)).forEach(articles::add);
        return articles;
    }
}
