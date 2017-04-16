package hu.bme.mit.textmine.rdf.service;

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

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.service.ArticleService;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.mongo.note.service.NoteService;
import hu.bme.mit.textmine.rdf.dal.DbpediaRepository;
import hu.bme.mit.textmine.rdf.dal.LocalRdfRepository;
import hu.bme.mit.textmine.rdf.model.RdfStatementsDTO;
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
                // save statements from remote db
                localRepository.save(statements);
                // connect to related documents
                // List<Document> documents =
                // this.documentService.getDocumentsByTitle(location);
                List<Document> documents = this.documentService.languageAgnosticFullTextQuery(location);
                String object = Iterables.getLast(statements).getSubject().stringValue();
                for (Document doc : documents) {
                    log.info("Found a matching document: " + doc.getIri());
                    this.localRepository.save(doc.getIri(), doc.getTitle(), this.vocabulary.document());
                    this.localRepository.addRelation(doc.getIri(), this.vocabulary.locationRelation(), object);
                }
                // connect to related articles
                // List<Article> articles =
                // this.articleService.getArticlesByEntryWord(location);
                List<Article> articles = this.articleService.languageAgnosticFullTextQuery(location);
                for (Article article : articles) {
                    log.info("Found a matching article: " + article.getIri() + ", " + article.getEntryWord());
                    this.localRepository.save(article.getIri(), article.getEntryWord(), this.vocabulary.article());
                    this.localRepository.addRelation(article.getIri(), this.vocabulary.locationRelation(), object);
                }
                // connect to related notes
                // List<Note> notes =
                // this.noteService.getNotesByQuote(location);
                List<Note> notes = this.noteService.languageAgnosticFullTextQuery(location);
                for (Note note : notes) {
                    log.info("Found a matching note: " + note.getIri() + ", " + note.getQuote());
                    this.localRepository.save(note.getIri(), note.getQuote(), this.vocabulary.note());
                    this.localRepository.addRelation(note.getIri(), this.vocabulary.locationRelation(), object);
                }
            }
        }
        log.info("All locations: " + locations.length + ", missing: " + missing + ", percentage: "
                + (double) (locations.length - missing) / (double) locations.length * 100 + "%");
        return RdfStatementsDTO.builder().resources(map).build();
    }

}
