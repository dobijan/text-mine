package hu.bme.mit.textmine.solr.model;

import java.util.List;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.google.common.collect.Lists;

import lombok.Builder;
import lombok.Data;

@SolrDocument(collection = "text-mine-formvariant")
@Data
@Builder
public class SolrFormVariant {

    @Id
    @Field
    private String id;

    @Field
    private String documentId;

    @Field
    private String articleId;

    @Field
    private String name;

    @Field(value = "inflections_inf")
    @Builder.Default
    private List<String> inflections = Lists.newArrayList();
}
