package hu.bme.mit.textmine.rdf.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EntityLinkCSVBean {

    private String entity;
    private String link;
}
