package hu.bme.mit.textmine.solr.dal.note;

import org.springframework.data.solr.repository.SolrCrudRepository;

import hu.bme.mit.textmine.solr.model.SolrNote;

public interface SolrNoteRepository extends SolrCrudRepository<SolrNote, String>, CustomSolrNoteRepository {

}
