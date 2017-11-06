package hu.bme.mit.textmine.solr.dal;

import java.util.List;

import org.springframework.data.solr.repository.SolrCrudRepository;

import hu.bme.mit.textmine.solr.model.SolrWord;

public interface SolrWordRepository extends SolrCrudRepository<SolrWord, String> {

    public List<SolrWord> findByDocumentId(String documentId);
}
