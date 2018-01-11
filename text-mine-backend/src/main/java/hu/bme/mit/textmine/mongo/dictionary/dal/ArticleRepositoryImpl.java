package hu.bme.mit.textmine.mongo.dictionary.dal;

import static hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy.CONTAINS;
import static hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy.ENDS_WITH;
import static hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy.STARTS_WITH;

import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.mongodb.client.result.UpdateResult;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.DocumentArticles;
import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;

@Repository
class ArticleRepositoryImpl implements CustomArticleRepository {

    @Resource
    @Lazy
    private MongoTemplate template;

    @Autowired
    @Lazy
    private DocumentService documentService;

    @Override
    public boolean updatePOS(String documentId, String entryWord, List<PartOfSpeech> pos) {
        Query query = new Query();
        query.addCriteria(Criteria.where("entryWord").is(entryWord).and("documentId").is(documentId));
        query.fields().include("entryWord");
        query.fields().include("documentId");
        Update update = new Update();
        update.set("partOfSpeech", pos);
        UpdateResult res = this.template.updateFirst(query, update, Article.class);
        return res.getModifiedCount() > 0;
    }

    @Override
    public Section getDocumentSection(String documentId, Integer sectionNumber) {
        if (!this.documentService.exists(documentId)) {
            return null;
        }
        return documentService.getSectionBySerial(documentId, sectionNumber);
    }

    @Override
    public Section getDocumentPage(String documentId, Integer sectionNumber) {
        if (!this.documentService.exists(documentId)) {
            return null;
        }
        return documentService.getPageBySerial(documentId, sectionNumber);
    }

    @Override
    public List<DocumentArticles> getArticlesByDocumentWithParams(String entryWord, String formVariant,
            String inflection, List<PartOfSpeech> partOfSpeech, MatchingStrategy matchingStrategy, Integer posCount) {
        List<AggregationOperation> steps = Lists.newArrayList();
        BiFunction<String, MatchingStrategy, String> patternFunction = (literal, strategy) -> {
            return strategy == STARTS_WITH ? literal + ".*"
                    : strategy == ENDS_WITH ? ".*" + literal
                            : strategy == CONTAINS ? ".*" + literal + ".*"
                                    : literal;
        };
        if (posCount != null) {
            steps.add(Aggregation.match(Criteria.where("partOfSpeech." + (posCount - 1)).exists(true)));
        }
        if (partOfSpeech != null) {
            steps.add(Aggregation.unwind("partOfSpeech"));
            steps.add(Aggregation.match(Criteria.where("partOfSpeech").in(partOfSpeech)));
        }
        if (entryWord != null) {
            steps.add(Aggregation
                    .match(Criteria.where("entryWord").regex(patternFunction.apply(entryWord, matchingStrategy))));
        }
        boolean formVarianUnwinded = false;
        if (formVariant != null) {
            formVarianUnwinded = true;
            steps.add(Aggregation.unwind("formVariants"));
            steps.add(Aggregation.match(
                    Criteria.where("formVariants.name").regex(patternFunction.apply(formVariant, matchingStrategy))));
        }
        if (inflection != null) {
            if (!formVarianUnwinded) {
                steps.add(Aggregation.unwind("formVariants"));
            }
            steps.add(Aggregation.unwind("formVariants.inflections"));
            steps.add(Aggregation.match(Criteria.where("formVariants.inflections.name")
                    .regex(patternFunction.apply(inflection, matchingStrategy))));
        }
        // so sad: https://jira.mongodb.org/browse/SERVER-14466
        // crazy workaround: group by whole DBRef
        steps.add(Aggregation.group("document").push(Aggregation.ROOT).as("articles"));
        TypedAggregation<Article> ta = Aggregation.newAggregation(Article.class, steps);
        return template.aggregate(ta, Article.class, DocumentArticles.class).getMappedResults();
    }
}
