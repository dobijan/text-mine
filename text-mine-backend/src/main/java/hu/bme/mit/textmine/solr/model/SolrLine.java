package hu.bme.mit.textmine.solr.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import lombok.Builder;
import lombok.Data;

@SolrDocument(collection = "text-mine-line")
@Data
@Builder
public class SolrLine {

    @Id
    @Field
    private String id;

    @Field
    private String documentId;

    @Field
    private String sectionId;

    @Field
    private long sectionSerial;

    @Field
    private long serial;

    @Field
    private String content;
}
