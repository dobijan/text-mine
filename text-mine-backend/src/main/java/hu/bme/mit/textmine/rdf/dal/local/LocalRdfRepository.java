package hu.bme.mit.textmine.rdf.dal.local;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import hu.bme.mit.textmine.rdf.model.SpatialQueryResult;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;

@Repository
public class LocalRdfRepository {

    private static final Pattern geoPointPattern = Pattern.compile("POINT\\((\\d+(?:\\.\\d+)?) (\\d+(?:\\.\\d+)?)\\)");

    @Autowired
    private TextMineVocabularyService vocabulary;

    @Value("${rdf.base.url}")
    private String SERVER_BASE_URL;

    @Value("${rdf.db}")
    private String DATABASE_NAME;

    private ValueFactory vf;

    // private SPARQLQueryRenderer renderer;

    private HTTPRepository repository;

    public LocalRdfRepository(@Value("${rdf.base.url}") String serverBaseUrl, @Value("${rdf.db}") String databaseName) {
        this.repository = new HTTPRepository(serverBaseUrl, databaseName);
        repository.initialize();
        // repository.setAdditionalHttpHeaders(ImmutableMap.of("Accept",
        // "application/sparql-results+xml"));
        this.vf = repository.getValueFactory();
        // this.renderer = new SPARQLQueryRenderer();
    }

    public void save(List<Statement> statements) {
        Repositories.consume(repository, conn -> conn.add(statements));
    }

    public Statement prepareRelation(String subject, String predicate, String object) {
        return vf.createStatement(vf.createIRI(subject), vf.createIRI(predicate), vf.createIRI(object),
                vf.createIRI(this.vocabulary.getBaseIri()));
    }

    public List<Statement> prepareResource(String subject, String label, String type) {
        List<Statement> statements = Lists.newArrayList();
        statements.add(this.vf.createStatement(vf.createIRI(subject), RDFS.LABEL, vf.createLiteral(label),
                vf.createIRI(this.vocabulary.getBaseIri())));
        statements.add(this.vf.createStatement(vf.createIRI(subject), RDF.TYPE, vf.createIRI(type),
                vf.createIRI(this.vocabulary.getBaseIri())));
        return statements;
    }

    public List<SpatialQueryResult> geoNear(String iri, int radiusInMeters) {
        List<SpatialQueryResult> results = Lists.newArrayList();
        String q = "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> "
                + "PREFIX ogc: <http://www.opengis.net/ont/geosparql#> "
                + "PREFIX geof: <http://www.opengis.net/def/function/geosparql/> "
                + "PREFIX uom: <http://www.opengis.net/def/uom/OGC/1.0/> "
                + "SELECT ?location (max(?distance) as ?maxDist) (group_concat(str(?point)) as ?coords) " + "WHERE { <"
                + vf.createIRI(iri) + "> geo:geometry ?center . " + " ?location geo:geometry ?point . "
                + "BIND( STRDT(str(?center), ogc:wktLiteral) as ?wktCenter) "
                + "BIND( STRDT(str(?point), ogc:wktLiteral) as ?wktPoint) "
                + "BIND( geof:distance(?wktCenter, ?wktPoint, uom:metre) as ?distance) "
                + "FILTER ( !sameTerm(?location, <" + vf.createIRI(iri) + ">) && ?distance <= " + radiusInMeters
                + ") } " + "GROUP BY ?location " + "ORDER BY ?maxDist";
        Repositories.tupleQuery(this.repository, q, QueryResults::asList).forEach(bindingSet -> {
            String geoNearCoordinates = bindingSet.getValue("coords").stringValue();
            if (geoNearCoordinates.equals("")) {
                return;
            }
            String geoNearIri = bindingSet.getValue("location").stringValue();
            String geoNearDistance = bindingSet.getValue("maxDist").stringValue();
            Matcher geoPointMatcher = geoPointPattern.matcher(geoNearCoordinates);
            if (geoPointMatcher.find()) {
                results.add(SpatialQueryResult.builder().from(iri).radius(radiusInMeters).iri(geoNearIri)
                        .distanceInMeters(Double.parseDouble(geoNearDistance))
                        .longitude(Double.parseDouble(geoPointMatcher.group(1)))
                        .latitude(Double.parseDouble(geoPointMatcher.group(2))).build());
            }
        });
        return results;
    }
}
