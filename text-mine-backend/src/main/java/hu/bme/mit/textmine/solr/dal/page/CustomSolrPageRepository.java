package hu.bme.mit.textmine.solr.dal.page;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.solr.model.SolrPage;
import hu.bme.mit.textmine.solr.model.WordTerm;

public interface CustomSolrPageRepository {

    public List<SolrPage> pagePhraseQuery(String documentId, String phrase);

    public List<SolrPage> phrasesQuery(String documentId, List<String> phrases, boolean exact, boolean disjoint);

    public List<SolrPage> partOfSpeechProximityQuery(String documentId, List<PartOfSpeech> pos, int slop);

    public SolrPage saveNoCommit(SolrPage entity);

    public Iterable<SolrPage> saveNoCommit(Collection<SolrPage> entities);

    public void commit();

    public List<SolrPage> phraseProximityQuery(Optional<String> documentId, List<WordTerm> terms, int slop,
            boolean mixed);

    public Map<String, Integer> mostFrequentShingles(String documentId, int limit);

    public Map<String, Integer> mostFrequentFilteredPosShingles(List<PartOfSpeech> pos, String documentId,
            int limit);
}
