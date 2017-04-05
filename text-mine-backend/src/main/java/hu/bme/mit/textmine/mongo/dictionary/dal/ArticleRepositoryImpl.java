package hu.bme.mit.textmine.mongo.dictionary.dal;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;

@Repository
public class ArticleRepositoryImpl implements CustomArticleRepository {
    
    @Resource
    private MongoTemplate template;

    @Override
    public boolean updatePOS(String entryWord, PartOfSpeech pos) {
        Query query = new Query();
        query.addCriteria(Criteria.where("entryWord").is(entryWord));
        query.fields().include("entryWord");
        Update update = new Update();
        update.set("partOfSpeech", pos);
        WriteResult res = this.template.updateFirst(query, update, Article.class);
        return res.getN() > 0;
    }
}
