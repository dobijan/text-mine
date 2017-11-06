package hu.bme.mit.textmine.solr.dal.document;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.solr.model.SolrDocument;
import hu.bme.mit.textmine.solr.model.WordTerm;

public interface CustomSolrDocumentRepository {

    public static final List<String> luceneSpecialCharacters = Lists.newArrayList("\\", "+", "-", "&&", "||", "!", "(",
            ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":");

    public List<SolrDocument> queryWithParams(String entryText, List<PartOfSpeech> partsOfSpeech,
            List<String> entryWords, List<String> documentIds, Integer page, Integer size);

    public List<SolrDocument> phrasesQuery(List<String> phrases, boolean exact, boolean disjoint);

    public static String escapePhrase(String phrase) {
        String s = new String(phrase);
        for (String specialChar : luceneSpecialCharacters) {
            s = s.replace(specialChar, "\\\\" + specialChar);
        }
        return s;
    }

    public List<SolrDocument> phraseProximityQuery(List<WordTerm> terms, int slop, boolean mixed);

    public List<SolrDocument> partOfSpeechProximityQuery(List<PartOfSpeech> pos, int slop);

    public Map<String, Integer> mostFrequentShingles(int limit);

    public Map<String, Integer> mostFrequentFilteredPosShingles(List<PartOfSpeech> pos, int limit);

    public SolrDocument saveNoCommit(SolrDocument entity);

    public Iterable<SolrDocument> saveNoCommit(Collection<SolrDocument> entities);

    public void commit();
}
