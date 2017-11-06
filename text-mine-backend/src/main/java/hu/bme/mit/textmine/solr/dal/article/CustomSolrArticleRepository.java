package hu.bme.mit.textmine.solr.dal.article;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.solr.core.query.result.GroupResult;

import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.solr.model.PartOfSpeechStatistics;
import hu.bme.mit.textmine.solr.model.SolrArticle;
import hu.bme.mit.textmine.solr.model.WordTerm;

public interface CustomSolrArticleRepository {

    public SolrArticle saveNoCommit(SolrArticle entity);

    public Iterable<SolrArticle> saveNoCommit(Collection<SolrArticle> entities);

    public void commit();

    public List<SolrArticle> findWithParams(String entryWord, String formVariant, String inflection,
            List<PartOfSpeech> partOfSpeech, List<String> documentIds, String corpusId,
            MatchingStrategy matchingStrategy, Integer offset, Integer limit);

    public GroupResult<SolrArticle> findWithParamsByDocumentId(String entryWord, String formVariant,
            String inflection, List<PartOfSpeech> partOfSpeech, MatchingStrategy matchingStrategy, Integer posCount);

    public List<SolrArticle> findByDocumentIdAndPOSOrderByFrequency(PartOfSpeech pos, List<String> documentIds,
            int page, int size);

    public PartOfSpeechStatistics getDocumentPOSStats(String documentId);

    public Long countArticlesByDocumentAndPOS(String documentId, PartOfSpeech pos);

    public List<SolrArticle> phrasesQuery(String documentId, List<String> phrases, boolean exact, boolean disjoint);

    public List<SolrArticle> phraseProximityQuery(Optional<String> documentId, List<WordTerm> terms, int slop);
}
