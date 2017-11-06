package hu.bme.mit.textmine.solr.dal.document;

import java.util.List;

import org.springframework.data.solr.repository.SolrCrudRepository;

import hu.bme.mit.textmine.solr.model.SolrDocument;

public interface SolrDocumentRepository extends SolrCrudRepository<SolrDocument, String>, CustomSolrDocumentRepository {

    public List<SolrDocument> findByCorpus(String corpus);
}
