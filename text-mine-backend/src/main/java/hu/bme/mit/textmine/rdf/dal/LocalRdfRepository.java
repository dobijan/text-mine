package hu.bme.mit.textmine.rdf.dal;

import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.queryrender.sparql.SPARQLQueryRenderer;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;

import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class LocalRdfRepository {

    @Autowired
    private TextMineVocabularyService vocabulary;

    @Value("${rdf.base.url}")
    private String SERVER_BASE_URL;

    @Value("${rdf.db}")
    private String DATABASE_NAME;

    private ValueFactory vf;

    private SPARQLQueryRenderer renderer;

    private SPARQLRepository repository;

    public LocalRdfRepository(@Value("${rdf.base.url}") String serverBaseUrl, @Value("${rdf.db}") String databaseName) {
        this.repository = new SPARQLRepository(String.join("/", serverBaseUrl, databaseName, "statements"));
        repository.initialize();
        repository.setAdditionalHttpHeaders(ImmutableMap.of("Accept", "application/rdf+xml"));
        this.vf = repository.getValueFactory();
        this.renderer = new SPARQLQueryRenderer();
    }

    public void save(List<Statement> statements) {
        Repositories.consume(repository, conn -> conn.add(statements));
    }

    public void addRelation(String subject, String predicate, String object) {
        this.save(Lists.newArrayList(vf.createStatement(vf.createIRI(subject), vf.createIRI(predicate),
                vf.createIRI(object), vf.createIRI(this.vocabulary.getBaseIri()))));
    }

    public void save(String subject, String label, String type) {
        List<Statement> statements = Lists.newArrayList();
        statements.add(this.vf.createStatement(vf.createIRI(subject), RDFS.LABEL, vf.createLiteral(label),
                vf.createIRI(this.vocabulary.getBaseIri())));
        statements.add(this.vf.createStatement(vf.createIRI(subject), RDF.TYPE, vf.createIRI(type),
                vf.createIRI(this.vocabulary.getBaseIri())));
        this.save(statements);
    }
}
