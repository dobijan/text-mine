package hu.bme.mit.textmine.solr.dal.line;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import hu.bme.mit.textmine.solr.model.SolrLine;
import hu.bme.mit.textmine.solr.model.WordTerm;

public interface CustomSolrLineRepository {

    public List<SolrLine> linePhraseQuery(String documentId, int sectionSerial, String phrase);

    public List<SolrLine> phrasesQuery(String documentId, Integer sectionSerial, List<String> phrases, boolean exact,
            boolean disjoint);

    public SolrLine saveNoCommit(SolrLine entity);

    public Iterable<SolrLine> saveNoCommit(Collection<SolrLine> entities);

    public void commit();

    public List<SolrLine> phraseProximityQuery(Optional<String> documentId, Optional<Long> sectionSerial,
            List<WordTerm> terms, int slop);

}
