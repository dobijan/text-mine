package hu.bme.mit.textmine.solr.dal.form_variant;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import hu.bme.mit.textmine.solr.model.SolrFormVariant;
import hu.bme.mit.textmine.solr.model.WordTerm;

public interface CustomSolrFormVariantRepository {

    public SolrFormVariant saveNoCommit(SolrFormVariant formVariant);

    public Iterable<SolrFormVariant> saveNoCommit(Collection<SolrFormVariant> formVariants);

    public void commit();

    public List<SolrFormVariant> phraseProximityQuery(Optional<String> documentId, Optional<String> articleId,
            List<WordTerm> terms, int slop);

    public List<SolrFormVariant> phrasesQuery(String documentId, String articleId, List<String> phrases, boolean exact,
            boolean disjoint);

}
