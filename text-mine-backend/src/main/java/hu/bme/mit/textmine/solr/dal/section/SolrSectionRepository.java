package hu.bme.mit.textmine.solr.dal.section;

import java.util.List;

import org.springframework.data.solr.repository.SolrCrudRepository;

import hu.bme.mit.textmine.solr.model.SolrSection;

public interface SolrSectionRepository extends SolrCrudRepository<SolrSection, String>, CustomSolrSectionRepository {

    public List<SolrSection> findByDocumentId(String documentId);
}
