package hu.bme.mit.textmine.rdf.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpatialQueryResult {

    private String from;
    private Integer radius;
    private String iri;
    private Double distanceInMeters;
    private Double latitude;
    private Double longitude;
}
