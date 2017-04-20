package hu.bme.mit.textmine.rdf.service;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanToCsv;
import com.opencsv.bean.ColumnPositionMappingStrategy;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.service.ArticleService;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.mongo.note.service.NoteService;
import hu.bme.mit.textmine.rdf.dal.DbpediaRepository;
import hu.bme.mit.textmine.rdf.dal.LocalRdfRepository;
import hu.bme.mit.textmine.rdf.model.EntityLinkCSVBean;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocationService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private DbpediaRepository dbpediaRepository;

    @Autowired
    private LocalRdfRepository localRepository;

    @Autowired
    private TextMineVocabularyService vocabulary;

    public boolean foundInDbpedia(String location) throws RepositoryException, MalformedQueryException, Exception {
        return !this.dbpediaRepository
                .findStatements(DbpediaRepository.getBaseUri() + URLEncoder.encode(location, "UTF-8")).isEmpty();
    }

    @SuppressWarnings("deprecation")
    @SneakyThrows(IOException.class)
    public String queryLocations(MultipartFile file) {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] locations = content.split("\\R+");
        int missing = 0;
        List<EntityLinkCSVBean> links = Lists.newArrayList();
        for (String location : locations) {
            List<Statement> statements = this.dbpediaRepository
                    .findStatements(DbpediaRepository.getBaseUri() + URLEncoder.encode(location, "UTF-8"));
            if (statements.isEmpty()) {
                ++missing;
                log.info("Missing location in dbpedia: " + location);
                links.add(EntityLinkCSVBean.builder().link("").entity(location).build());
            } else {
                links.add(EntityLinkCSVBean.builder()
                        .link(DbpediaRepository.getBaseUri() + URLEncoder.encode(location, "UTF-8")).entity(location)
                        .build());
                // save statements from remote db
                localRepository.save(statements);
                // connect to related documents
                List<Document> documents = this.documentService.languageAgnosticFullTextQuery(location);
                Set<String> subjects = statements.stream().map(s -> s.getSubject().stringValue())
                        .collect(Collectors.toSet());
                for (Document doc : documents) {
                    log.info("Found a matching document for " + location + ": " + doc.getTitle());
                    this.localRepository.save(doc.getIri(), doc.getTitle(), this.vocabulary.document());
                    subjects.forEach(
                            s -> this.localRepository.addRelation(doc.getIri(), this.vocabulary.locationRelation(), s));
                }
                // connect to related articles
                List<Article> articles = this.articleService.languageAgnosticFullTextQuery(location);
                for (Article article : articles) {
                    log.info("Found a matching article for " + location + ": " + article.getEntryWord());
                    this.localRepository.save(article.getIri(), article.getEntryWord(), this.vocabulary.article());
                    subjects.forEach(s -> this.localRepository.addRelation(article.getIri(),
                            this.vocabulary.locationRelation(), s));
                }
                // connect to related notes
                List<Note> notes = this.noteService.languageAgnosticFullTextQuery(location);
                for (Note note : notes) {
                    log.info("Found a matching note for " + location + ": " + note.getQuote());
                    this.localRepository.save(note.getIri(), note.getQuote(), this.vocabulary.note());
                    subjects.forEach(s -> this.localRepository.addRelation(note.getIri(),
                            this.vocabulary.locationRelation(), s));
                }
            }
        }
        log.info("All locations: " + locations.length + ", missing: " + missing + ", percentage: "
                + (double) (locations.length - missing) / (double) locations.length * 100 + "%");
        try (StringWriter sw = new StringWriter(); CSVWriter writer = new CSVWriter(sw, ';')) {
            BeanToCsv<EntityLinkCSVBean> btc = new BeanToCsv<>();
            ColumnPositionMappingStrategy<EntityLinkCSVBean> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(EntityLinkCSVBean.class);
            strategy.setColumnMapping("entity", "link");
            btc.write(strategy, writer, links);
            return sw.toString();
        }
    }
}
