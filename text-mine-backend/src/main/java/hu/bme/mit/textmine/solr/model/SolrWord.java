package hu.bme.mit.textmine.solr.model;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import lombok.Builder;
import lombok.Data;

@SolrDocument(collection = "text-mine-word")
@Data
@Builder
public class SolrWord {

    @Id
    @Field
    private String id;

    @Field
    private String documentId;

    @Field
    private String sectionId;

    @Field
    private String lineId;

    @Field
    private String content;

    @Field("pos")
    private String partsOfSpeech;
}
