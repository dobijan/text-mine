package hu.bme.mit.textmine.solr.dal.article;

import java.util.List;

import org.springframework.data.solr.repository.SolrCrudRepository;

import hu.bme.mit.textmine.solr.model.SolrArticle;

public interface SolrArticleRepository extends SolrCrudRepository<SolrArticle, String>, CustomSolrArticleRepository {

    public List<SolrArticle> findByDocumentId(String documentId);
}
