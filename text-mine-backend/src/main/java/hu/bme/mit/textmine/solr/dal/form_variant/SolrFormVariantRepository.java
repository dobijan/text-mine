package hu.bme.mit.textmine.solr.dal.form_variant;

import java.util.List;

import org.springframework.data.solr.repository.SolrCrudRepository;

import hu.bme.mit.textmine.solr.model.SolrFormVariant;

public interface SolrFormVariantRepository
        extends SolrCrudRepository<SolrFormVariant, String>, CustomSolrFormVariantRepository {

    public List<SolrFormVariant> findByDocumentId(String documentId);

    public List<SolrFormVariant> findByArticleId(String articleId);
}
