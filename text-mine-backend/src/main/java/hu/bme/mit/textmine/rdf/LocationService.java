package hu.bme.mit.textmine.rdf;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocationService {

    @Autowired
    private DbpediaRepository dbpediaRepository;

    @Autowired
    private LocalRdfRepository localRepository;
    
    public boolean foundInDbpedia(String location) throws RepositoryException, MalformedQueryException, Exception {
        return !this.dbpediaRepository
                .findStatements(DbpediaRepository.getBaseUri() + URLEncoder.encode(location, "UTF-8")).isEmpty();
    }

    public RdfStatementsDTO queryLocations(MultipartFile file)
            throws RepositoryException, MalformedQueryException, Exception {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] locations = content.split("\\R+");
        Map<String, List<Statement>> map = Maps.newHashMap();
        int missing = 0;
        for (String location : locations) {
            List<Statement> statements = this.dbpediaRepository
                    .findStatements(DbpediaRepository.getBaseUri() + URLEncoder.encode(location, "UTF-8"));
            map.put(location, statements);
            if (statements.isEmpty()) {
                ++missing;
                log.info(location);
            } else {
//                localRepository.save(statements);
            }
        }
        log.info("All locations: " + locations.length + ", missing: " + missing + ", percentage: "
                + (double) (locations.length - missing) / (double) locations.length * 100 + "%");
        return RdfStatementsDTO.builder().resources(map).build();
    }
}
