package hu.bme.mit.textmine.solr.dal.section;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.solr.model.SolrSection;
import hu.bme.mit.textmine.solr.model.WordTerm;

public interface CustomSolrSectionRepository {

    public List<SolrSection> sectionPhraseQuery(String documentId, String phrase);

    public List<SolrSection> phrasesQuery(String documentId, List<String> phrases, boolean exact, boolean disjoints);

    public List<SolrSection> partOfSpeechProximityQuery(String documentId, List<PartOfSpeech> pos, int slop);

    public SolrSection saveNoCommit(SolrSection entity);

    public Iterable<SolrSection> saveNoCommit(Collection<SolrSection> entities);

    public void commit();

    public List<SolrSection> phraseProximityQuery(Optional<String> documentId, List<WordTerm> terms, int slop,
            boolean mixed);

    public Map<String, Integer> mostFrequentShingles(String documentId, int limit);

    public Map<String, Integer> mostFrequentFilteredPosShingles(List<PartOfSpeech> pos, String documentId,
            int limit);
}
