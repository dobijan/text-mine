package hu.bme.mit.textmine.rdf.dal.dbpedia;

import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.algebra.Compare.CompareOp;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilderFactory;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class DbpediaRepository {

    private static final String URL = "http://dbpedia.org/sparql";

    private static final String BASE_URI = "http://dbpedia.org/resource/";

    private static final String ONTOLOGY_BASE_URI = "http://dbpedia.org/ontology/";

    private static final Set<String> TRAVERSAL_ATTRIBUTES = Sets
            .newHashSet("http://dbpedia.org/ontology/wikiPageRedirects", "http://www.w3.org/2002/07/owl#sameAs");

    public static String getBaseUri() {
        return BASE_URI;
    }

    public static String getOntologyBaseUri() {
        return ONTOLOGY_BASE_URI;
    }

    private ValueFactory vf;

    private SPARQLQueryRenderer renderer;

    private SPARQLRepository repository;

    public DbpediaRepository() {
        this.repository = new SPARQLRepository(URL);
        repository.initialize();
        // repository.setAdditionalHttpHeaders(ImmutableMap.of("Accept", "text/plain"));
        repository.setAdditionalHttpHeaders(ImmutableMap.of("Accept", "application/rdf+xml"));
        // RDFFormat f = RDFFormat.RDFJSON;
        this.vf = repository.getValueFactory();
        this.renderer = new SPARQLQueryRenderer();
    }

    public List<Statement> findStatements(String iri) {
        List<Statement> statements = Lists.newArrayList();
        try (RepositoryConnection conn = this.repository.getConnection()) {
            log.info("Connection established.");
            statements.addAll(this.matchSubGraphWithTraversal(iri, conn, 5));
        }
        log.info("Connection closed.");
        return statements;
    }

    @SneakyThrows
    private List<Statement> matchSubGraphWithTraversal(String iri, RepositoryConnection conn, int depth) {
        if (depth < 1) {
            return Lists.newArrayList();
        }
        List<Statement> statements = Lists.newArrayList();
        ParsedQuery q = QueryBuilderFactory.construct().addProjectionVar("s", "p", "o").group().atom("s", "p", "o")
                .filter("s", CompareOp.EQ, vf.createIRI(iri)).closeGroup().query();
        GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, renderer.render(q));
        log.info("Query prepared with depth " + depth);
        try (GraphQueryResult result = graphQuery.evaluate()) {
            log.info("Query evaluated.");
            while (result.hasNext()) {
                Statement stmt = result.next();
                Statement stmtWithContext = vf.createStatement(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(),
                        vf.createIRI(BASE_URI));
                statements.add(stmtWithContext);
                if (TRAVERSAL_ATTRIBUTES.stream()
                        .anyMatch(attribute -> stmt.getPredicate().stringValue().equalsIgnoreCase(attribute))) {
                    statements.addAll(matchSubGraphWithTraversal(stmt.getObject().stringValue(), conn, --depth));
                }
            }
        }
        return statements;
    }
}
