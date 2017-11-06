package hu.bme.mit.textmine.solr.dal.inflection;

import java.util.List;

import org.springframework.data.solr.repository.SolrCrudRepository;

import hu.bme.mit.textmine.solr.model.SolrInflection;

public interface SolrInflectionRepository
        extends SolrCrudRepository<SolrInflection, String>, CustomSolrInflectionRepository {

    public List<SolrInflection> findByDocumentId(String documentId);

    public List<SolrInflection> findByArticleId(String articleId);
}
