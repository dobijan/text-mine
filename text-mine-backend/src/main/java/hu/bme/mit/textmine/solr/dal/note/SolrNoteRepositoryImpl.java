package hu.bme.mit.textmine.solr.dal.note;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.DefaultQueryParser;
import org.springframework.data.solr.core.QueryParser;
import org.springframework.data.solr.core.RequestMethod;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;

import com.google.common.collect.Lists;

import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.solr.dal.document.CustomSolrDocumentRepository;
import hu.bme.mit.textmine.solr.model.SolrNote;
import hu.bme.mit.textmine.solr.model.WordTerm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SolrNoteRepositoryImpl implements CustomSolrNoteRepository {

    @Autowired
    private SolrOperations template;

    private QueryParser qp = new DefaultQueryParser();

    @Override
    public List<SolrNote> phraseQuery(Optional<String> documentId, Optional<Integer> section, List<String> phrases,
            boolean exact, boolean disjoint) {
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
        if (documentId.isPresent()) {
            searchCriteria = searchCriteria.and(new Criteria("documentId").is(documentId.get()));
        }
        if (section.isPresent()) {
            searchCriteria = searchCriteria.and(new Criteria("section").is((long) section.get().intValue()));
        }
        Query q = new SimpleQuery(searchCriteria).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (anyComplex && !exact) {
            q.setDefType("complexphrase");
        }
        log.info("Constructed Solr query for notes: " + qp.getQueryString(q));
        return this.template.queryForPage("text-mine-note", q, SolrNote.class, RequestMethod.POST).getContent();
    }

    @Override
    public SolrNote saveNoCommit(SolrNote entity) {
        this.template.saveBean("text-mine-note", entity, Duration.ofMinutes(10));
        return entity;
    }

    @Override
    public Iterable<SolrNote> saveNoCommit(Collection<SolrNote> entities) {
        this.template.saveBeans("text-mine-note", entities, Duration.ofMinutes(10));
        return entities;
    }

    @Override
    public void commit() {
        this.template.commit("text-mine-note");
    }

    @Override
    public List<SolrNote> phraseProximityQuery(Optional<String> documentId, Optional<Long> section,
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
        if (section.isPresent()) {
            c = c.and(new Criteria("section").is(section.get()));
        }
        Query q = new SimpleQuery(c).setPageRequest(PageRequest.of(0, Integer.MAX_VALUE));
        if (terms.stream().map(WordTerm::getStrategy).anyMatch(s -> !s.equals(MatchingStrategy.EXACT_MATCH))
                && terms.size() > 1) {
            q.setDefType("complexphrase");
        }
        return this.template.queryForPage("text-mine-note", q, SolrNote.class, RequestMethod.POST)
                .getContent();
    }

}
