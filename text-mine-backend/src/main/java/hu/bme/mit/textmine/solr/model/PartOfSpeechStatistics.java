package hu.bme.mit.textmine.solr.model;

import java.util.Map;

import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartOfSpeechStatistics {

    private String documentId;

    private Map<PartOfSpeech, Long> count;

    private Map<PartOfSpeech, Double> proportions;
}
