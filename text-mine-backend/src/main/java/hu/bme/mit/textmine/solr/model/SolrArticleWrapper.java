package hu.bme.mit.textmine.solr.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolrArticleWrapper {

    private SolrArticle article;

    private List<SolrFormVariant> formVariants;

    private List<SolrInflection> inflections;
}
