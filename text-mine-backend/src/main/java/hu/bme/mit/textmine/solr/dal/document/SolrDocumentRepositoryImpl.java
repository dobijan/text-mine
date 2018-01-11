package hu.bme.mit.textmine.solr.dal.document;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.data.solr.core.query.SimpleStringCriteria;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.google.common.collect.Lists;

import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.solr.model.SolrDocument;
import hu.bme.mit.textmine.solr.model.WordTerm;

public class SolrDocumentRepositoryImpl implements CustomSolrDocumentRepository {

    @Autowired
    private SolrOperations template;

    @Override
    public List<SolrDocument> queryWithParams(String entryText, List<PartOfSpeech> partsOfSpeech,
            List<String> entryWords, List<String> documentIds, Integer page, Integer size) {
        List<Criteria> criteria = Lists.newArrayList();
        Boolean complex = entryText.matches(".*[\\s\\p{Z}]+.*");
        if (StringUtils.isNotBlank(entryText)) {
            if (complex) {
                criteria.add(new Criteria("text")
                        .expression("\"*" + CustomSolrDocumentRepository.escapePhrase(entryText) + "*\""));
            } else {
                criteria.add(new Criteria("text").contains(entryText));
            }
        }
        if (documentIds != null) {
            criteria.add(new Criteria("id").in(documentIds));
        }
        for (String entryWord : entryWords) {
            criteria.add(new Criteria("text").contains(entryWord));
        }
        for (PartOfSpeech pos : partsOfSpeech) {
            criteria.add(new Criteria("pos").contains(pos.toString()));
        }
        Criteria searchCriteria = null;
        for (Criteria c : criteria) {
            if (searchCriteria == null) {
                searchCriteria = c;
            } else {
                searchCriteria = searchCriteria.and(c);
            }
        }
        Query q = new SimpleQuery(searchCriteria).setPageRequest(PageRequest.of(page, size));
        if (complex) {
            q.setDefType("complexphrase");
        }
        ScoredPage<SolrDocument> result = this.template.queryForPage("text-mine-document", q, SolrDocument.class,
                RequestMethod.POST);

        return result.getContent();
    }

    @Override
    public List<SolrDocument> phrasesQuery(List<String> phrases, boolean exact, boolean disjoint) {
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
        Query q = new SimpleQuery(searchCriteria).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (anyComplex && !exact) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-document", q, SolrDocument.class, RequestMethod.POST).getContent();
    }

    @Override
    public List<SolrDocument> phraseProximityQuery(List<WordTerm> terms, int slop, boolean mixed) {
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
        Query q = new SimpleQuery(c).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (terms.stream().map(WordTerm::getStrategy).anyMatch(s -> !s.equals(MatchingStrategy.EXACT_MATCH))
                && terms.size() > 1) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-document", q, SolrDocument.class, RequestMethod.POST).getContent();
    }

    @Override
    public List<SolrDocument> partOfSpeechProximityQuery(List<PartOfSpeech> pos, int slop) {
        if (slop < 0) {
            return Lists.newArrayList();
        }
        String expression = pos.stream().map(p -> "*\\[" + p.toString() + "\\]*").collect(Collectors.joining(" "));
        Criteria c = new Criteria("pos").expression(expression);
        Query q = new SimpleQuery(c).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (pos.size() > 1) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-document", q, SolrDocument.class, RequestMethod.POST).getContent();
    }

    @Override
    public Map<String, Integer> mostFrequentShingles(int limit) {
        FacetQuery q = new SimpleFacetQuery(new SimpleStringCriteria("*:*")).setFacetOptions(
                new FacetOptions("text_phrases").setFacetLimit(limit));
        FacetPage<SolrDocument> page = this.template.queryForFacetPage("text-mine-document", q, SolrDocument.class,
                RequestMethod.POST);
        Map<String, Integer> result = Maps.newHashMap();
        page.getFacetResultPage("text_phrases").getContent().stream()
                .forEach(entry -> result.put(entry.getValue(), (int) entry.getValueCount()));
        return result;
    }

    @Override
    public Map<String, Integer> mostFrequentFilteredPosShingles(List<PartOfSpeech> partsOfSpeech, int limit) {
        if (partsOfSpeech == null || partsOfSpeech.isEmpty()) {
            return Maps.newHashMap();
        }
        List<Map<String, Integer>> shingleSets = Lists.newArrayList();
        for (PartOfSpeech pos : partsOfSpeech) {
            FacetOptions options = new FacetOptions("pos_shingles").setFacetLimit(limit);
            FacetQuery q = new SimpleFacetQuery(new SimpleStringCriteria("*:*")).setFacetOptions(options);
            SolrQuery solrQuery = new DefaultQueryParser().constructSolrQuery(q);
            solrQuery.add("facet.contains", pos.toString().toLowerCase());
            String queryString = new DefaultQueryParser().getQueryString(q);
            solrQuery.set(CommonParams.Q, queryString);
            Map<String, Integer> terms = this.template.execute(new SolrCallback<Map<String, Integer>>() {

                @Override
                public Map<String, Integer> doInSolr(SolrClient solrClient)
                        throws SolrServerException, IOException {
                    QueryResponse response = solrClient.query("text-mine-document", solrQuery);
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

    @Override
    public SolrDocument saveNoCommit(SolrDocument entity) {
        this.template.saveBean("text-mine-document", entity, Duration.ofMinutes(10));
        return entity;
    }

    @Override
    public Iterable<SolrDocument> saveNoCommit(Collection<SolrDocument> entities) {
        this.template.saveBeans("text-mine-document", entities, Duration.ofMinutes(10));
        return entities;
    }

    @Override
    public void commit() {
        this.template.commit("text-mine-document");
    }
}
