package hu.bme.mit.textmine.solr.dal.inflection;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleField;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;

import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.solr.dal.document.CustomSolrDocumentRepository;
import hu.bme.mit.textmine.solr.model.SolrInflection;
import hu.bme.mit.textmine.solr.model.WordTerm;

@Repository
public class SolrInflectionRepositoryImpl implements CustomSolrInflectionRepository {

    @Autowired
    // @Qualifier("text-mine-inflection-template")
    private SolrOperations template;

    // @PostConstruct
    // public void init() {
    // this.template = new SolrTemplate(solrClient, "text-mine-inflection");
    // this.template.setSolrConverter(new MappingSolrConverter(new SimpleSolrMappingContext()));
    // }

    @Override
    public SolrInflection saveNoCommit(SolrInflection inflection) {
        this.template.saveBean("text-mine-inflection", inflection, Duration.ofMinutes(10));
        return inflection;
    }

    @Override
    public Iterable<SolrInflection> saveNoCommit(Collection<SolrInflection> inflections) {
        this.template.saveBeans("text-mine-inflection", inflections, Duration.ofMinutes(10));
        return inflections;
    }

    @Override
    public void commit() {
        this.template.commit("text-mine-inflection");
    }

    @Override
    public List<String> findInText(String text) {
        List<String> tokens = Arrays.asList(text.split("\\s+")).stream().map(String::toLowerCase).distinct()
                .collect(Collectors.toList());
        List<Criteria> criteria = Lists.newArrayList();
        for (String token : tokens) {
            criteria.add(new Criteria("name").is(token));
        }
        Criteria searchCriteria = null;
        for (Criteria c : criteria) {
            if (searchCriteria == null) {
                searchCriteria = c;
            } else {
                searchCriteria = searchCriteria.or(c);
            }
        }
        Field f = new SimpleField("articleId");
        Query q = new SimpleQuery(searchCriteria)
                .setGroupOptions(new GroupOptions()
                        .addGroupByField(f)
                        .setLimit(Integer.MAX_VALUE)
                        .setOffset(0)
                        .setTotalCount(true))
                .setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        GroupPage<SolrInflection> groups = this.template.queryForGroupPage("text-mine-inflection", q,
                SolrInflection.class, RequestMethod.POST);
        return groups.getGroupResult(f).getGroupEntries().stream().map(g -> g.getGroupValue())
                .collect(Collectors.toList());
    }

    @Override
    public long countByName(String name) {
        Criteria criteria = new Criteria("name").is(name);
        Query q = new SimpleQuery(criteria);
        return this.template.count("text-mine-inflection", q, RequestMethod.POST);
    }

    @Override
    public List<SolrInflection> phraseProximityQuery(Optional<String> documentId, Optional<String> articleId,
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
        return this.template.queryForPage("text-mine-inflection", q, SolrInflection.class, RequestMethod.POST)
                .getContent();
    }

    @Override
    public List<SolrInflection> phrasesQuery(String documentId, String articleId, List<String> phrases, boolean exact,
            boolean disjoint) {
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
        return this.template.queryForPage("text-mine-inflection", q, SolrInflection.class, RequestMethod.POST)
                .getContent();
    }
}
