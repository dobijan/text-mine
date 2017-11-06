package hu.bme.mit.textmine.solr.dal.section;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.DefaultQueryParser;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrCallback;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FacetOptions;
import org.springframework.data.solr.core.query.FacetQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFacetQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.FacetPage;

import com.google.common.collect.Lists;

import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.solr.dal.document.CustomSolrDocumentRepository;
import hu.bme.mit.textmine.solr.dal.document.SolrDocumentRepository;
import hu.bme.mit.textmine.solr.model.SolrSection;
import hu.bme.mit.textmine.solr.model.WordTerm;

public class SolrSectionRepositoryImpl implements CustomSolrSectionRepository {

    @Autowired
    private SolrOperations template;

    @Autowired
    @Lazy
    private SolrDocumentRepository documentRepository;

    @Override
    public List<SolrSection> sectionPhraseQuery(String documentId, String phrase) {
        boolean complex = phrase.matches(".*[\\s\\p{Z}]+.*");
        Criteria c = new Criteria("documentId").is(documentId)
                .and(complex
                        ? new Criteria("text")
                                .expression("\"*" + CustomSolrDocumentRepository.escapePhrase(phrase) + "*\"")
                        : new Criteria("text").contains(phrase));
        Query q = new SimpleQuery(c).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        return this.template.queryForPage("text-mine-section", q, SolrSection.class, RequestMethod.POST).getContent();
    }

    @Override
    public List<SolrSection> phrasesQuery(String documentId, List<String> phrases, boolean exact, boolean disjoint) {
        List<Criteria> criteria = Lists.newArrayList();
        boolean anyComplex = false;
        String wildcard = exact ? "" : "*";
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
            searchCriteria = new Criteria("documentId").is(documentId).and(searchCriteria);
        }
        Query q = new SimpleQuery(searchCriteria).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (anyComplex && !exact) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-section", q, SolrSection.class, RequestMethod.POST).getContent();
    }

    @Override
    public List<SolrSection> partOfSpeechProximityQuery(String documentId, List<PartOfSpeech> pos, int slop) {
        if (slop < 0) {
            return Lists.newArrayList();
        }
        String expression = pos.stream().map(p -> "*\\[" + p.toString() + "\\]*").collect(Collectors.joining(" "));
        Criteria c = new Criteria("pos").expression(expression).and(new Criteria("documentId").is(documentId));
        Query q = new SimpleQuery(c).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (pos.size() > 1) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-section", q, SolrSection.class, RequestMethod.POST).getContent();
    }

    @Override
    public SolrSection saveNoCommit(SolrSection entity) {
        this.template.saveBean("text-mine-section", entity, Duration.ofMinutes(10));
        return entity;
    }

    @Override
    public Iterable<SolrSection> saveNoCommit(Collection<SolrSection> entities) {
        this.template.saveBeans("text-mine-section", entities, Duration.ofMinutes(10));
        return entities;
    }

    @Override
    public void commit() {
        this.template.commit("text-mine-section");
    }

    @Override
    public List<SolrSection> phraseProximityQuery(Optional<String> documentId, List<WordTerm> terms, int slop,
            boolean mixed) {
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
        Criteria c = new Criteria(mixed ? "pos" : "text").expression(phraseBuilder.toString());
        if (documentId.isPresent()) {
            c = c.and(new Criteria("documentId").is(documentId.get()));
        }
        Query q = new SimpleQuery(c).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (terms.stream().map(WordTerm::getStrategy).anyMatch(s -> !s.equals(MatchingStrategy.EXACT_MATCH))
                && terms.size() > 1) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-section", q, SolrSection.class, RequestMethod.POST).getContent();
    }

    @Override
    public Map<String, Integer> mostFrequentShingles(String documentId, int limit) {
        FacetQuery q = new SimpleFacetQuery(new Criteria("documentId").is(documentId)).setFacetOptions(
                new FacetOptions("text_phrases").setFacetLimit(limit));
        FacetPage<SolrSection> page = this.template.queryForFacetPage("text-mine-section", q, SolrSection.class,
                RequestMethod.POST);
        Map<String, Integer> result = Maps.newHashMap();
        page.getFacetResultPage("text_phrases").getContent().stream()
                .forEach(entry -> result.put(entry.getValue(), (int) entry.getValueCount()));
        return result;
    }

    @Override
    public Map<String, Integer> mostFrequentFilteredPosShingles(List<PartOfSpeech> partsOfSpeech,
            String documentId, int limit) {
        if (partsOfSpeech == null || partsOfSpeech.isEmpty() || documentId == null) {
            return Maps.newHashMap();
        }
        List<Map<String, Integer>> shingleSets = Lists.newArrayList();
        for (PartOfSpeech pos : partsOfSpeech) {
            FacetOptions options = new FacetOptions("pos_shingles").setFacetLimit(limit);
            FacetQuery q = new SimpleFacetQuery(new Criteria("documentId").is(documentId)).setFacetOptions(options);
            SolrQuery solrQuery = new DefaultQueryParser().constructSolrQuery(q);
            solrQuery.add("facet.contains", pos.toString().toLowerCase());
            String queryString = new DefaultQueryParser().getQueryString(q);
            solrQuery.set(CommonParams.Q, queryString);
            Map<String, Integer> terms = this.template.execute(new SolrCallback<Map<String, Integer>>() {

                @Override
                public Map<String, Integer> doInSolr(SolrClient solrClient)
                        throws SolrServerException, IOException {
                    QueryResponse response = solrClient.query("text-mine-section", solrQuery);
                    List<FacetField> fflist = response.getFacetFields();
                    Map<String, Integer> terms = Maps.newHashMap();
                    for (FacetField ff : fflist) {
                        if (ff.getName().equals("pos_shingles")) {
                            for (Count c : ff.getValues()) {
                                terms.put(c.getName(), (int) c.getCount());
                            }
                        }
                    }
                    return terms;
                }
            });
            shingleSets.add(terms);
        }
        Map<String, Integer> smallestSet = shingleSets.stream()
                .sorted((s1, s2) -> Integer.compare(s1.size(), s2.size()))
                .findFirst().get();
        Map<String, Integer> resultMap = Maps.newHashMap();
        for (String key : smallestSet.keySet()) {
            if (partsOfSpeech.stream().allMatch(pos -> key.contains(pos.toString().toLowerCase()))) {
                resultMap.put(key, smallestSet.get(key));
            }
        }
        return resultMap;
    }
}
