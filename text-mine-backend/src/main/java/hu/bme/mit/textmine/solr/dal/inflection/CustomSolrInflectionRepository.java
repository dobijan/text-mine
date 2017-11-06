package hu.bme.mit.textmine.solr.dal.inflection;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import hu.bme.mit.textmine.solr.model.SolrInflection;
import hu.bme.mit.textmine.solr.model.WordTerm;

public interface CustomSolrInflectionRepository {

    public SolrInflection saveNoCommit(SolrInflection inflection);

    public Iterable<SolrInflection> saveNoCommit(Collection<SolrInflection> inflections);

    public void commit();

    public List<String> findInText(String text);

    public long countByName(String name);

    public List<SolrInflection> phraseProximityQuery(Optional<String> documentId, Optional<String> articleId,
            List<WordTerm> terms, int slop);

    public List<SolrInflection> phrasesQuery(String documentId, String articleId, List<String> phrases, boolean exact,
            boolean disjoint);

}
