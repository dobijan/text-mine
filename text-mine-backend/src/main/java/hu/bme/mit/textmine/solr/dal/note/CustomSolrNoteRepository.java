package hu.bme.mit.textmine.solr.dal.note;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import hu.bme.mit.textmine.solr.model.SolrNote;
import hu.bme.mit.textmine.solr.model.WordTerm;

public interface CustomSolrNoteRepository {

    public List<SolrNote> phraseQuery(Optional<String> documentId, Optional<Integer> section, List<String> phrases,
            boolean exact, boolean disjoint);

    public SolrNote saveNoCommit(SolrNote entity);

    public Iterable<SolrNote> saveNoCommit(Collection<SolrNote> entities);

    public void commit();

    public List<SolrNote> phraseProximityQuery(Optional<String> documentId, Optional<Long> section,
            List<WordTerm> terms, int slop);
}
