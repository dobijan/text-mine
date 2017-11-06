package hu.bme.mit.textmine.solr.dal.page;

import java.util.List;

import org.springframework.data.solr.repository.SolrCrudRepository;

import hu.bme.mit.textmine.solr.model.SolrPage;

public interface SolrPageRepository extends SolrCrudRepository<SolrPage, String>, CustomSolrPageRepository {

    public List<SolrPage> findByDocumentId(String documentId);
}
