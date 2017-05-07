package hu.bme.mit.textmine.rdf.service;

import java.util.List;

import org.springframework.stereotype.Service;

import hu.bme.mit.textmine.rdf.model.SpatialQueryResult;

@Service
public class LocationService extends RdfResourceService {

    @Override
    protected void setRelationType() {
        this.relationType = this.vocabulary.locationRelation();
    }

    public List<SpatialQueryResult> geoNearQuery(String iri, Integer radiusInMeters) {
        return this.localRepository.geoNear(iri, radiusInMeters);
    }
}
