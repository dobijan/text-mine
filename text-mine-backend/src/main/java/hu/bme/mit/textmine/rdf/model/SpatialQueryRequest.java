package hu.bme.mit.textmine.rdf.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpatialQueryRequest {

    private String iri;
    private Integer radius;
}
