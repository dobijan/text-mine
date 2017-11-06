package hu.bme.mit.textmine.solr.model;

import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WordTerm {

    private MatchingStrategy strategy;
    private String term;
}
