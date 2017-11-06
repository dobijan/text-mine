package hu.bme.mit.textmine.solr.dal.form_variant;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Repository;

import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.solr.dal.document.CustomSolrDocumentRepository;
import hu.bme.mit.textmine.solr.model.SolrFormVariant;
import hu.bme.mit.textmine.solr.model.WordTerm;

@Repository
public class SolrFormVariantRepositoryImpl implements CustomSolrFormVariantRepository {

    @Autowired
    // @Qualifier("text-mine-formvariant-template")
    private SolrOperations template;

    // @PostConstruct
    // public void init() {
    // this.template = new SolrTemplate(solrClient, "text-mine-formvariant");
    // this.template.setSolrConverter(new MappingSolrConverter(new SimpleSolrMappingContext()));
    // }

    @Override
    public SolrFormVariant saveNoCommit(SolrFormVariant formVariant) {
        this.template.saveBean("text-mine-formvariant", formVariant, Duration.ofMinutes(10));
        return formVariant;
    }

    @Override
    public Iterable<SolrFormVariant> saveNoCommit(Collection<SolrFormVariant> formVariants) {
        this.template.saveBeans("text-mine-formvariant", formVariants, Duration.ofMinutes(10));
        return formVariants;
    }

    @Override
    public void commit() {
        this.template.commit("text-mine-formvariant");
    }

    @Override
    public List<SolrFormVariant> phraseProximityQuery(Optional<String> documentId, Optional<String> articleId,
            List<WordTerm> terms, int slop) {
        if (slop < 0) {
            return Lists.newArrayList();
        }
        StringBuilder phraseBuilder = new StringBuilder();
        phraseBuilder.append("\"");
        List<String> termPatterns = Lists.newArrayList();
        for (WordTerm term : terms) {
            StringBuilder termBuilder = new StringBuilder();
            if (MatchingStrategy.CONTAINS.equals(term.getStrategy())
                    || MatchingStrategy.ENDS_WITH.equals(term.getStrategy())) {
                termBuilder.append("*");
            }
            termBuilder.append(CustomSolrDocumentRepository.escapePhrase(term.getTerm()));
            if (MatchingStrategy.CONTAINS.equals(term.getStrategy())
                    || MatchingStrategy.STARTS_WITH.equals(term.getStrategy())) {
                termBuilder.append("*");
            }
            termPatterns.add(termBuilder.toString());
        }
        phraseBuilder.append(String.join(" ", termPatterns));
        phraseBuilder.append("\"");
        if (slop > 0) {
            phraseBuilder.append("~" + Integer.toString(slop));
        }
        Criteria c = new Criteria("text").expression(phraseBuilder.toString());
        if (documentId.isPresent()) {
            c = c.and(new Criteria("documentId").is(documentId.get()));
        }
        if (articleId.isPresent()) {
            c = c.and(new Criteria("articleId").is(articleId.get()));
        }
        Query q = new SimpleQuery(c).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (terms.stream().map(WordTerm::getStrategy).anyMatch(s -> !s.equals(MatchingStrategy.EXACT_MATCH))
                && terms.size() > 1) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-formvariant", q, SolrFormVariant.class, RequestMethod.POST)
                .getContent();
    }

    @Override
    public List<SolrFormVariant> phrasesQuery(String documentId, String articleId, List<String> phrases,
            boolean exact, boolean disjoint) {
        List<Criteria> criteria = Lists.newArrayList();
        String wildcard = exact ? "" : "*";
        boolean anyComplex = false;
        for (String phrase : phrases) {
            boolean complex = phrase.matches(".*[\\s\\p{Z}]+.*");
            if (complex) {
                criteria.add(
                        new Criteria("text")
                                .expression("\"" + wildcard + CustomSolrDocumentRepository.escapePhrase(phrase)
                                        + wildcard + "\""));
                anyComplex = true;
            } else {
                criteria.add(exact ? new Criteria("text").is(phrase) : new Criteria("text").contains(phrase));
            }
        }
        Criteria searchCriteria = null;
        for (Criteria c : criteria) {
            if (searchCriteria == null) {
                searchCriteria = c;
            } else {
                if (disjoint) {
                    searchCriteria = searchCriteria.or(c);
                } else {
                    searchCriteria = searchCriteria.and(c);
                }
            }
        }
        if (documentId != null) {
            searchCriteria = searchCriteria.and(new Criteria("documentId").is(documentId));
        }
        if (articleId != null) {
            searchCriteria = searchCriteria.and(new Criteria("articleId").is(articleId));
        }
        Query q = new SimpleQuery(searchCriteria).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (anyComplex && !exact) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-formvariant", q, SolrFormVariant.class, RequestMethod.POST)
                .getContent();
    }
}
