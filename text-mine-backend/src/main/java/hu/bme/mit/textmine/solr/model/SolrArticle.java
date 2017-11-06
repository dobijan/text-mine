package hu.bme.mit.textmine.solr.model;

import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import lombok.Builder;
import lombok.Data;

@SolrDocument(collection = "text-mine-article")
@Data
@Builder
public class SolrArticle {

    @Id
    @Field
    private String id;

    @Field
    private String documentId;

    @Field
    private String entryWord;

    @Field
    private Boolean properNoun;

    @Field
    private Boolean derivative;

    @Field
    private String editorNote;

    @Field
    private String meaning;

    @Field(value = "partOfSpeech_pos")
    @Builder.Default
    private List<String> partOfSpeech = Lists.newArrayList();

    @Field(value = "externalReferences_exref")
    @Builder.Default
    private List<String> externalReferences = Lists.newArrayList();

    @Field(value = "internalReferences_intref")
    @Builder.Default
    private List<String> internalReferences = Lists.newArrayList();

    @Field(value = "formvariants_fv")
    @Builder.Default
    private List<String> formVariants = Lists.newArrayList();

    @Field(value = "inflections_inf")
    @Builder.Default
    private List<String> inflections = Lists.newArrayList();

    @Field
    @Builder.Default
    private long frequency = 0;
}
