package hu.bme.mit.textmine.solr.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import lombok.Builder;
import lombok.Data;

@SolrDocument(collection = "text-mine-page")
@Data
@Builder
public class SolrPage {

    @Id
    @Field
    private String id;

    @Field
    private String documentId;

    @Field
    private long serial;

    @Field
    private String content;

    @Field
    private String normalized;

    @Field("pos")
    private String partsOfSpeech;
}
