package hu.bme.mit.textmine.rdf.dal.sztaki;

import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;

@Repository
public class SztakiLodRepository {

    private static final String URL = "http://lod.sztaki.hu/sparql";

    @Getter
    private static final String ITEM_BASE_URI = "http://lod.sztaki.hu/data/item/";

    @Getter
    private static final String PERSON_BASE_URI = "http://lod.sztaki.hu/data/auth/";

    // private static final Set<String> TRAVERSAL_ATTRIBUTES = Sets.newHashSet("http://www.w3.org/2002/07/owl#sameAs");
    //
    // private ValueFactory vf;
    //
    // private SPARQLQueryRenderer renderer;

    private SPARQLRepository repository;

    public SztakiLodRepository() {
        this.repository = new SPARQLRepository(URL);
        repository.initialize();
        repository.setAdditionalHttpHeaders(ImmutableMap.of("Accept", "application/rdf+xml"));
        // this.vf = repository.getValueFactory();
        // this.renderer = new SPARQLQueryRenderer();
    }

    // public List<Statement> findPerson(String foafName) {
    // ParsedQuery q = QueryBuilderFactory.construct().addProjectionVar("s", "p", "o").group().atom("s", "p", "o")
    // .atom("s", FOAF.NAME, foafName).filter("").closeGroup().query();
    // Repositories.graphQuery(this.repository, "", result -> {
    //
    // });
    // }
}
