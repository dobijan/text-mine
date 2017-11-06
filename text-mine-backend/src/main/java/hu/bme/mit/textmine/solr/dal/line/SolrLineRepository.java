package hu.bme.mit.textmine.solr.dal.line;

import java.util.List;

import org.springframework.data.solr.repository.SolrCrudRepository;

import hu.bme.mit.textmine.solr.model.SolrLine;

public interface SolrLineRepository extends SolrCrudRepository<SolrLine, String>, CustomSolrLineRepository {

    public List<SolrLine> findByDocumentId(String documentId);
}
