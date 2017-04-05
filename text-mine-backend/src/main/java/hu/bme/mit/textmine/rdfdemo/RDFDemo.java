package hu.bme.mit.textmine.rdfdemo;

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
import org.eclipse.rdf4j.repository.util.Repositories;

import com.google.common.collect.Lists;

public class RDFDemo {

	public static void main(String[] args) throws RepositoryException, MalformedQueryException, Exception {
		String rdfUrl = "http://dbpedia.org/sparql";
		SPARQLRepository repository = new SPARQLRepository(rdfUrl);
		repository.initialize();
		ValueFactory vf = repository.getValueFactory();
		SPARQLQueryRenderer renderer = new SPARQLQueryRenderer();
		List<Statement> statements = Lists.newArrayList();
		try (RepositoryConnection conn = repository.getConnection()) {
			ParsedQuery q = QueryBuilderFactory.construct().addProjectionVar("s", "p", "o").group().atom("s", "p", "o")
					.filter("s", CompareOp.EQ, vf.createIRI("http://dbpedia.org/resource/Sándor_Petőfi")).closeGroup()
					.query();
			System.out.println(renderer.render(q));
			// TupleQuery tupleQuery =
			// conn.prepareTupleQuery(QueryLanguage.SPARQL, renderer.render(q));
			GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, renderer.render(q));
			try (GraphQueryResult result = graphQuery.evaluate()) {
				while (result.hasNext()) { // iterate over the result
					Statement stmt = result.next();
					statements.add(stmt);
					System.out.println(stmt.getSubject() + " | " + stmt.getPredicate().stringValue() + " | "
							+ stmt.getObject().stringValue());
				}
			}
		}
		String localUrl = "http://localhost:8080/rdf4j-server/repositories/test/statements";
		repository = new SPARQLRepository(localUrl);
		repository.initialize();
		Repositories.consume(repository, conn -> conn.add(statements));
	}

}
