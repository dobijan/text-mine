package hu.bme.mit.textmine.solr.model;

import java.util.List;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.google.common.collect.Lists;

import lombok.Builder;
import lombok.Data;

@SolrDocument(collection = "text-mine-note")
@Data
@Builder
public class SolrNote {

    @Id
    @Field
    private String id;

    @Field
    private String documentId;

    @Field
    private String type;

    @Field
    private String subType;

    @Field
    private String content;

    @Field
    private String quote;

    @Field
    private Long section;

    @Field(value = "lineRefs_refs")
    @Builder.Default
    private List<Long> lineRefs = Lists.newArrayList();
}
