package hu.bme.mit.textmine.rdf;

import java.util.List;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.algebra.Compare.CompareOp;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.queryrender.builder.QueryBuilderFactory;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@Repository
public class DbpediaRepository {

    private static final String URL      = "http://dbpedia.org/sparql";
    private static final String BASE_URI = "http://dbpedia.org/resource/";
    
    public static String getBaseUri() {
        return BASE_URI;
    }

    private ValueFactory vf;

    private SPARQLQueryRenderer renderer;

    private SPARQLRepository repository;

    public DbpediaRepository() {
        this.repository = new SPARQLRepository(URL);
        repository.initialize();
        repository.setAdditionalHttpHeaders(ImmutableMap.of("Accept", "application/rdf+xml"));
        this.vf = repository.getValueFactory();
        this.renderer = new SPARQLQueryRenderer();
    }

    public List<Statement> findStatements(String uri) throws RepositoryException, MalformedQueryException, Exception {
        List<Statement> statements = Lists.newArrayList();
        try (RepositoryConnection conn = this.repository.getConnection()) {
            ParsedQuery q = QueryBuilderFactory.construct().addProjectionVar("s", "p", "o").group().atom("s", "p", "o")
                    .filter("s", CompareOp.EQ, vf.createIRI(uri)).closeGroup().query();
            GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, renderer.render(q));
            try (GraphQueryResult result = graphQuery.evaluate()) {
                while (result.hasNext()) { // iterate over the result
                    Statement stmt = result.next();
                    statements.add(stmt);
                }
            }
        }
        return statements;
    }
}
