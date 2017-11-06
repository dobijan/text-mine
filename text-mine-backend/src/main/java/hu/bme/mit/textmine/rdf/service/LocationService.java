package hu.bme.mit.textmine.rdf.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import hu.bme.mit.textmine.mongo.core.RdfEntity;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.QueryHits;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.rdf.dal.dbpedia.DbpediaRepository;
import hu.bme.mit.textmine.rdf.model.Location;
import hu.bme.mit.textmine.rdf.model.SpatialQueryResult;
import hu.bme.mit.textmine.rdf.model.Triple;

@Service
public class LocationService extends RdfResourceService {

    @Autowired
    @Lazy
    private DocumentService documentService;

    @Override
    protected void setRelationType() {
        this.relationType = this.vocabulary.locationRelation();
    }

    @Override
    protected RdfEntity createResource(String resource) {
        Location location = Location.builder().name(resource).build();
        location.setIri(this.vocabulary.asResource(location));
        return location;
    }

    @Override
    protected void setResourceType() {
        this.resourceType = this.vocabulary.location();
    }

    @Override
    protected String getLabel(RdfEntity entity) {
        return ((Location) entity).getName();
    }

    public List<SpatialQueryResult> geoNearQuery(String iri, Integer radiusInMeters) {
        return this.localRepository.geoNear(iri, radiusInMeters);
    }

    public List<SpatialQueryResult> sectionLocationVicinity(String iri, Integer radiusInMeters) {
        List<String> locations = this.localRepository.resourcesForEntity(iri, this.vocabulary.locationRelation(),
                this.vocabulary.section());
        List<String> remoteLocations = Lists.newArrayList();
        for (String location : locations) {
            remoteLocations.addAll(this.localRepository.resourcesForEntity(location,
                    this.vocabulary.sameResourceRelation(), this.vocabulary.location()));
        }
        Set<SpatialQueryResult> results = Sets.newHashSet();
        for (String locationIri : remoteLocations) {
            results.addAll(geoNearQuery(locationIri, radiusInMeters));
        }
        return Lists.newArrayList(results);
    }

    // @SneakyThrows(UnsupportedEncodingException.class)
    public List<String> getIrisForLocation(String location) {
        return this.irisForLabel(location);
    }

    public List<Line> linesForLocationIri(String documentId, Integer sectionSerial, List<String> iris) {
        return this.linesForIri(documentId, sectionSerial, iris);
    }

    public List<Section> sectionsForLocationIri(String documentId, List<String> iris) {
        return this.sectionsForIri(documentId, iris);
    }

    public List<Section> pagesForLocationIri(String documentId, List<String> iris) {
        return this.pagesForIri(documentId, iris);
    }

    public List<Note> notesForLocationIri(String documentId, List<String> iris) {
        return this.notesForIri(documentId, iris);
    }

    public QueryHits<Line> linesForCountry(String documentId, Integer sectionSerial, String country) {
        List<Triple> predicates = Lists.newArrayList(
                Triple.of("subject", RDF.TYPE, this.localRepository.getVf().createIRI(this.vocabulary.line())),
                Triple.of("subject", this.localRepository.getVf().createIRI(this.vocabulary.locationRelation()),
                        "location"),
                Triple.of("location", this.localRepository.getVf().createIRI(this.vocabulary.sameResourceRelation()),
                        "remoteLocation"),
                Triple.of("remoteLocation",
                        this.localRepository.getVf().createIRI(DbpediaRepository.getOntologyBaseUri() + "country"),
                        this.localRepository.getVf().createIRI(DbpediaRepository.getBaseUri() + country)));
        List<Triple> notePredicates = Lists.newArrayList(
                Triple.of("subject", RDF.TYPE, this.localRepository.getVf().createIRI(this.vocabulary.note())),
                Triple.of("subject", this.localRepository.getVf().createIRI(this.vocabulary.locationRelation()),
                        "location"),
                Triple.of("location", this.localRepository.getVf().createIRI(this.vocabulary.sameResourceRelation()),
                        "remoteLocation"),
                Triple.of("remoteLocation",
                        this.localRepository.getVf().createIRI(DbpediaRepository.getOntologyBaseUri() + "country"),
                        this.localRepository.getVf().createIRI(DbpediaRepository.getBaseUri() + country)));
        List<String> iris = this.localRepository.entitiesForPredicates(predicates, "subject");
        List<Line> baseHits = documentService.lineIriQuery(documentId, sectionSerial, iris);
        List<String> noteIris = this.localRepository.entitiesForPredicates(notePredicates, "subject");
        List<Note> noteHits = noteService.getNotesByIriAndDocumentId(documentId, noteIris);
        List<Long> lineSerials = noteHits.stream().map(Note::getLineRefs)
                .flatMap(Set::stream)
                .map(Long::new)
                .collect(Collectors.toList());
        return QueryHits.<Line>builder()
                .baseHits(Sets.newHashSet(baseHits))
                .noteHits(Sets.newHashSet(this.documentService.getLinesBySectionAndLineNumbers(documentId,
                        sectionSerial, lineSerials)))
                .build();
    }

    public QueryHits<Line> linesForLocation(String documentId, Integer sectionSerial, String location) {
        List<String> iris = getIrisForLocation(location);
        if (!iris.isEmpty()) {
            List<Line> directHits = linesForLocationIri(documentId, sectionSerial, iris);
            List<Note> noteHits = notesForIri(documentId, iris);
            List<Long> lineSerials = noteHits.stream().map(Note::getLineRefs)
                    .flatMap(Set::stream)
                    .map(Long::new)
                    .collect(Collectors.toList());
            return QueryHits.<Line>builder()
                    .baseHits(Sets.newHashSet(directHits))
                    .noteHits(Sets.newHashSet(this.documentService.getLinesBySectionAndLineNumbers(documentId,
                            sectionSerial, lineSerials)))
                    .build();
        } else {
            return QueryHits.<Line>builder().build();
        }
    }

    public QueryHits<Section> sectionsForLocation(String documentId, String location) {
        List<String> iris = getIrisForLocation(location);
        if (!iris.isEmpty()) {
            List<Section> directHits = sectionsForLocationIri(documentId, iris);
            List<Note> noteHits = notesForIri(documentId, iris);
            return QueryHits.<Section>builder()
                    .baseHits(Sets.newHashSet(directHits))
                    .noteHits(Sets.newHashSet(this.documentService.getSectionsBySerial(documentId,
                            noteHits.stream().map(n -> (long) n.getSection()).collect(Collectors.toList()))))
                    .build();
        } else {
            return QueryHits.<Section>builder().build();
        }
    }

    public List<Section> pagesForLocation(String documentId, String location) {
        List<String> iris = getIrisForLocation(location);
        if (!iris.isEmpty()) {
            return pagesForLocationIri(documentId, iris);
        } else {
            return Lists.newArrayList();
        }
    }
}
