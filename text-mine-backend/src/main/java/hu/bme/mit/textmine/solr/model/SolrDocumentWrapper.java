package hu.bme.mit.textmine.solr.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolrDocumentWrapper {

    private SolrDocument document;

    private List<SolrSection> sections;

    private List<SolrPage> pages;

    private List<SolrLine> lines;

    private List<SolrWord> words;
}
