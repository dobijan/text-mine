package hu.bme.mit.textmine.solr.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;

import lombok.Builder;
import lombok.Data;

@org.springframework.data.solr.core.mapping.SolrDocument(collection = "text-mine-document")
@Data
@Builder
public class SolrDocument {

    @Id
    @Field
    private String id;

    @Field
    private String corpus;

    @Field
    private String title;

    @Field
    private String content;

    @Field
    private String normalized;

    @Field("pos")
    private String partsOfSpeech;

    @Field
    private String author;
}
