package hu.bme.mit.textmine.solr.dal.article;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleField;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;

import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.solr.dal.document.CustomSolrDocumentRepository;
import hu.bme.mit.textmine.solr.dal.document.SolrDocumentRepository;
import hu.bme.mit.textmine.solr.model.PartOfSpeechStatistics;
import hu.bme.mit.textmine.solr.model.SolrArticle;
import hu.bme.mit.textmine.solr.model.WordTerm;
import lombok.NonNull;

@Repository
public class SolrArticleRepositoryImpl implements CustomSolrArticleRepository {

    @Autowired
    // @Qualifier("text-mine-article-template")
    private SolrOperations template;

    @Autowired
    private SolrDocumentRepository documentRepository;

    // @PostConstruct
    // public void init() {
    // this.template = new SolrTemplate(solrClient, "text-mine-article");
    // this.template.setSolrConverter(new MappingSolrConverter(new SimpleSolrMappingContext()));
    // }

    @Override
    public SolrArticle saveNoCommit(SolrArticle article) {
        this.template.saveBean("text-mine-article", article, Duration.ofMinutes(10));
        return article;
    }

    @Override
    public Iterable<SolrArticle> saveNoCommit(Collection<SolrArticle> articles) {
        this.template.saveBeans("text-mine-article", articles, Duration.ofMinutes(10));
        return articles;
    }

    @Override
    public void commit() {
        this.template.commit("text-mine-article");
    }

    @Override
    public List<SolrArticle> findWithParams(String entryWord, String formVariant, String inflection,
            List<PartOfSpeech> partOfSpeech, List<String> documentIds, String corpusId,
            @NonNull MatchingStrategy matchingStrategy, @NonNull Integer page, @NonNull Integer size) {
        Criteria searchCriteria = this.createSearchCriteria(entryWord, formVariant, inflection, partOfSpeech,
                matchingStrategy);
        if (documentIds != null) {
            searchCriteria.and(new Criteria("documentId").in(documentIds));
        }
        if (StringUtils.isNotBlank(corpusId)) {
            List<String> docIds = this.documentRepository.findByCorpus(corpusId).stream().map(d -> d.getId())
                    .collect(Collectors.toList());
            searchCriteria.and(new Criteria("documentId").in(docIds));
        }
        Query query = new SimpleQuery(searchCriteria, PageRequest.of(page, size));
        ScoredPage<SolrArticle> solrPage = this.template.queryForPage("text-mine-article", query, SolrArticle.class,
                RequestMethod.POST);
        return solrPage.getContent();
    }

    @Override
    public GroupResult<SolrArticle> findWithParamsByDocumentId(String entryWord, String formVariant,
            String inflection, List<PartOfSpeech> partOfSpeech, MatchingStrategy matchingStrategy, Integer posCount) {
        Criteria searchCriteria = this.createSearchCriteria(entryWord, formVariant, inflection, partOfSpeech,
                matchingStrategy);
        if (posCount != null) {
            searchCriteria.and(new Criteria("partOfSpeech_pos_count").is(posCount));
        }
        Field field = new SimpleField("documentId");
        Query query = new SimpleQuery(searchCriteria)
                .setGroupOptions(new GroupOptions()
                        .setTotalCount(true)
                        .addGroupByField(field)
                        .setLimit(Integer.MAX_VALUE)
                        .setOffset(0))
                .setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        GroupPage<SolrArticle> solrPage = this.template.queryForGroupPage("text-mine-article", query, SolrArticle.class,
                RequestMethod.POST);
        return solrPage.getGroupResult(field);
    }

    private Criteria createSearchCriteria(String entryWord, String formVariant, String inflection,
            List<PartOfSpeech> partOfSpeech, @NonNull MatchingStrategy matchingStrategy) {
        List<Criteria> criteria = Lists.newArrayList();
        if (StringUtils.isNotBlank(entryWord)) {
            switch (matchingStrategy) {
                case EXACT_MATCH:
                    criteria.add(new Criteria("entryWord").is(entryWord));
                    break;
                case STARTS_WITH:
                    criteria.add(new Criteria("entryWord").startsWith(entryWord));
                    break;
                case ENDS_WITH:
                    criteria.add(new Criteria("entryWord").endsWith(entryWord));
                    break;
                case CONTAINS:
                    criteria.add(new Criteria("entryWord").contains(entryWord));
            }
        }
        if (StringUtils.isNotBlank(formVariant)) {
            switch (matchingStrategy) {
                case EXACT_MATCH:
                    criteria.add(new Criteria("formvariants_fv").is(formVariant));
                    break;
                case STARTS_WITH:
                    criteria.add(new Criteria("formvariants_fv").startsWith(formVariant));
                    break;
                case ENDS_WITH:
                    criteria.add(new Criteria("formvariants_fv").endsWith(formVariant));
                    break;
                case CONTAINS:
                    criteria.add(new Criteria("formvariants_fv").contains(formVariant));
            }
        }
        if (StringUtils.isNotBlank(inflection)) {
            switch (matchingStrategy) {
                case EXACT_MATCH:
                    criteria.add(new Criteria("inflections_inf").is(inflection));
                    break;
                case STARTS_WITH:
                    criteria.add(new Criteria("inflections_inf").startsWith(inflection));
                    break;
                case ENDS_WITH:
                    criteria.add(new Criteria("inflections_inf").endsWith(inflection));
                    break;
                case CONTAINS:
                    criteria.add(new Criteria("inflections_inf").contains(inflection));
            }
        }
        if (partOfSpeech != null) {
            criteria.add(new Criteria("partOfSpeech_pos")
                    .in(partOfSpeech.stream().map(pos -> pos.toString()).collect(Collectors.toList())));
        }
        Criteria searchCriteria = null;
        for (Criteria c : criteria) {
            if (searchCriteria == null) {
                searchCriteria = c;
            } else {
                searchCriteria = searchCriteria.and(c);
            }
        }
        return searchCriteria;
    }

    @Override
    public List<SolrArticle> findByDocumentIdAndPOSOrderByFrequency(PartOfSpeech pos, List<String> documentIds,
            int page, int size) {
        Criteria c = new Criteria("partOfSpeech_pos").is(pos.toString())
                .and(new Criteria("documentId").in(documentIds));
        Query q = new SimpleQuery(c).addSort(Sort.by(Direction.DESC, "frequency"))
                .setPageRequest(PageRequest.of(page, size));
        return this.template.queryForPage("text-mine-article", q, SolrArticle.class, RequestMethod.POST).getContent();
    }

    @Override
    public PartOfSpeechStatistics getDocumentPOSStats(String documentId) {
        Map<PartOfSpeech, Long> counts = Maps.newHashMap();
        Map<PartOfSpeech, Double> proportions = Maps.newHashMap();
        Long count = 0L;
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            Long posCount = this.countArticlesByDocumentAndPOS(documentId, pos);
            counts.put(pos, posCount);
            count += posCount;
        }
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            if (count != 0) {
                proportions.put(pos, (double) counts.get(pos) / (double) count);
            } else {
                proportions.put(pos, 0.0);
            }
        }
        return PartOfSpeechStatistics.builder()
                .documentId(documentId)
                .count(counts)
                .proportions(proportions)
                .build();
    }

    @Override
    public Long countArticlesByDocumentAndPOS(String documentId, PartOfSpeech pos) {
        Criteria c = new Criteria("documentId").is(documentId).and(new Criteria("partOfSpeech_pos").is(pos.toString()));
        Query q = new SimpleQuery(c);
        return this.template.count("text-mine-article", q, RequestMethod.POST);
    }

    @Override
    public List<SolrArticle> phrasesQuery(String documentId, List<String> phrases, boolean exact, boolean disjoint) {
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
        Query q = new SimpleQuery(searchCriteria).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (anyComplex && !exact) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-article", q, SolrArticle.class, RequestMethod.POST).getContent();
    }

    @Override
    public List<SolrArticle> phraseProximityQuery(Optional<String> documentId, List<WordTerm> terms, int slop) {
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
        Query q = new SimpleQuery(c).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (terms.stream().map(WordTerm::getStrategy).anyMatch(s -> !s.equals(MatchingStrategy.EXACT_MATCH))
                && terms.size() > 1) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-article", q, SolrArticle.class, RequestMethod.POST).getContent();
    }
}
