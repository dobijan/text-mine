package hu.bme.mit.textmine.rdf.dal.local;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.algebra.Compare.CompareOp;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilder;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilderFactory;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.zalando.fauxpas.FauxPas;

import hu.bme.mit.textmine.rdf.model.SpatialQueryResult;
import hu.bme.mit.textmine.rdf.model.Triple;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class LocalRdfRepository {

    private static final Pattern geoPointPattern = Pattern.compile("POINT\\((\\d+(?:\\.\\d+)?) (\\d+(?:\\.\\d+)?)\\)");

    @Autowired
    private TextMineVocabularyService vocabulary;

    @Value("${rdf.base.url}")
    private String SERVER_BASE_URL;

    @Value("${rdf.db}")
    private String DATABASE_NAME;

    @Getter
    private ValueFactory vf;

    private SPARQLQueryRenderer renderer;

    private HTTPRepository repository;

    public LocalRdfRepository(@Value("${rdf.base.url}") String serverBaseUrl, @Value("${rdf.db}") String databaseName) {
        this.repository = new HTTPRepository(serverBaseUrl, databaseName);
        repository.initialize();
        // repository.setAdditionalHttpHeaders(ImmutableMap.of("Accept",
        // "application/sparql-results+xml"));
        this.vf = repository.getValueFactory();
        this.renderer = new SPARQLQueryRenderer();
    }

    @SneakyThrows(Exception.class)
    public boolean subjectExists(String iri) {
        ParsedQuery q = QueryBuilderFactory.construct().addProjectionVar("s", "p", "o").group().atom("s", "p", "o")
                .filter("s", CompareOp.EQ, vf.createIRI(iri)).closeGroup().query();
        return Repositories.graphQuery(repository, renderer.render(q), QueryResults::singleResult) != null;
    }

    public void save(List<Statement> statements) {
        // Repositories.consume(repository, conn -> conn.add(statements));
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.begin(IsolationLevels.NONE);
            try {
                conn.add(statements);
                log.info("Statements added.");
                conn.commit();
                log.info("Transaction committed.");
            } catch (RepositoryException e) {
                log.error("Transaction rolled back: " + e.getStackTrace());
                conn.rollback();
            }
        }
    }

    public Statement prepareRelation(String subject, String predicate, String object) {
        return vf.createStatement(vf.createIRI(subject), vf.createIRI(predicate), vf.createIRI(object),
                vf.createIRI(this.vocabulary.getBaseIri()));
    }

    public List<Statement> prepareResource(String subject, String label, String type, List<String> alternativeNames) {
        List<Statement> statements = Lists.newArrayList();
        statements.add(this.vf.createStatement(vf.createIRI(subject), RDFS.LABEL, vf.createLiteral(label),
                vf.createIRI(this.vocabulary.getBaseIri())));
        statements.add(this.vf.createStatement(vf.createIRI(subject), RDF.TYPE, vf.createIRI(type),
                vf.createIRI(this.vocabulary.getBaseIri())));
        for (String alternative : alternativeNames) {
            statements.add(
                    this.vf.createStatement(vf.createIRI(subject),
                            vf.createIRI(this.vocabulary.nameAlternativeRelation()), vf.createLiteral(alternative),
                            vf.createIRI(this.vocabulary.getBaseIri())));
        }
        return statements;
    }

    public List<Statement> createSameAsResources(String iri, Set<String> sameAsResources) {
        List<Statement> statements = Lists.newArrayList();
        for (String sameAsResource : sameAsResources) {
            statements.add(this.vf.createStatement(vf.createIRI(iri),
                    vf.createIRI(this.vocabulary.sameResourceRelation()), vf.createIRI(sameAsResource),
                    vf.createIRI(this.vocabulary.getBaseIri())));
        }
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
        for (BindingSet bindingSet : Repositories.tupleQuery(this.repository, q,
                FauxPas.throwingFunction(QueryResults::asList))) {
            String geoNearCoordinates = bindingSet.getValue("coords").stringValue();
            if (geoNearCoordinates.equals("")) {
                continue;
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
        }
        return results;
    }

    public List<String> getIriForResourceName(String name, String resourceType) {
        List<String> results = Lists.newArrayList();
        String q = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "select distinct ?s where { " +
                "  ?s ?p ?o . " +
                "  ?s rdf:type <" + resourceType + "> . " +
                "    {?s rdfs:label \"" + name + "\"} " +
                "  UNION " +
                "    {?s <" + this.vocabulary.nameAlternativeRelation() + "> \"" + name + "\"} " +
                "  UNION " +
                "    {?s <" + this.vocabulary.sameResourceRelation() + "> ?s2 . " +
                "     ?s2 rdfs:label \"" + name + "\" . } . " +
                "}";
        Repositories.tupleQuery(this.repository, q, FauxPas.throwingFunction(QueryResults::asList))
                .forEach(bindingSet -> results.add(bindingSet.getValue("s").toString()));
        return results;
    }

    @SneakyThrows(Exception.class)
    public List<String> entitiesForResource(List<String> iris, String relationType, String entityType) {
        List<String> result = Lists.newArrayList();
        // ParsedQuery q = QueryBuilderFactory.select("subject").group()
        // .atom("subject", RDF.TYPE, vf.createIRI(entityType)).closeGroup().group()
        // .atom("subject", vf.createIRI(relationType), vf.createIRI(iri)).closeGroup().query();
        String iriCondition = String.join(" ", iris.stream().map(iri -> "<" + iri + ">").collect(Collectors.toList()));
        String q = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "select distinct ?s where { " +
                "  ?s rdf:type <" + entityType + "> . " +
                "  VALUES ?iri { " + iriCondition + " } " +
                "  ?s <" + relationType + "> ?iri . " +
                "}";
        Repositories.tupleQuery(this.repository, q, FauxPas.throwingFunction(QueryResults::asList))
                .forEach(bindingSet -> {
                    result.add(bindingSet.getValue("s").stringValue());
                });
        return result;
    }

    @SneakyThrows(Exception.class)
    public List<String> resourcesForEntity(String iri, String relationType, String entityType) {
        List<String> result = Lists.newArrayList();
        ParsedQuery q = QueryBuilderFactory.select("object").group()
                .atom(vf.createIRI(iri), RDF.TYPE, vf.createIRI(entityType)).closeGroup().group()
                .atom(vf.createIRI(iri), vf.createIRI(relationType), "object").closeGroup().query();
        Repositories
                .tupleQuery(this.repository, this.renderer.render(q), FauxPas.throwingFunction(QueryResults::asList))
                .forEach(bindingSet -> {
                    result.add(bindingSet.getValue("object").stringValue());
                });
        return result;
    }

    @SneakyThrows(Exception.class)
    public List<String> entitiesForPredicates(List<Triple> predicates, String varName) {
        List<String> result = Lists.newArrayList();
        QueryBuilder<ParsedTupleQuery> qb = QueryBuilderFactory.select(varName);
        for (Triple predicate : predicates) {
            if (predicate.getObject() instanceof org.eclipse.rdf4j.model.Value) {
                qb = qb.group().atom(predicate.getSubject(), predicate.getPredicate(),
                        (org.eclipse.rdf4j.model.Value) predicate.getObject())
                        .closeGroup();
            } else {
                qb = qb.group().atom(predicate.getSubject(), predicate.getPredicate(),
                        (String) predicate.getObject())
                        .closeGroup();
            }
        }
        Repositories
                .tupleQuery(this.repository, this.renderer.render(qb.query()),
                        FauxPas.throwingFunction(QueryResults::asList))
                .forEach(bindingSet -> {
                    result.add(bindingSet.getValue(varName).stringValue());
                });
        return result;
    }
}
