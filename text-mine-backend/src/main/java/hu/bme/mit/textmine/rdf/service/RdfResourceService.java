package hu.bme.mit.textmine.rdf.service;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.mongo.note.service.NoteService;
import hu.bme.mit.textmine.rdf.dal.dbpedia.DbpediaRepository;
import hu.bme.mit.textmine.rdf.dal.local.LocalRdfRepository;
import hu.bme.mit.textmine.rdf.model.EntityLinkCSVBean;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public abstract class RdfResourceService {

    @Autowired
    protected DocumentService documentService;

    @Autowired
    protected ArticleService articleService;

    @Autowired
    protected NoteService noteService;

    @Autowired
    protected DbpediaRepository dbpediaRepository;

    @Autowired
    protected LocalRdfRepository localRepository;

    @Autowired
    protected TextMineVocabularyService vocabulary;

    @Getter
    protected String relationType;

    protected abstract void setRelationType();

    @PostConstruct
    public void init() {
        this.setRelationType();
    }

    public boolean foundInDbpedia(String location) throws RepositoryException, MalformedQueryException, Exception {
        return !this.dbpediaRepository
                .findStatements(DbpediaRepository.getBaseUri() + URLEncoder.encode(location, "UTF-8")).isEmpty();
    }

    protected void connectDocuments(List<Statement> statements, List<String> phrases, String regex,
            Set<String> subjects) {
        log.info("Full text document query for: " + regex);
        Set<Document> documents = this.documentService.languageAgnosticFullTextQuery(phrases);
        log.info("Found " + documents.size() + " documents.");
        for (Document doc : documents) {
            statements.addAll(
                    this.localRepository.prepareResource(doc.getIri(), doc.getTitle(), this.vocabulary.document()));
            subjects.forEach(s -> statements.add(this.localRepository.prepareRelation(doc.getIri(), relationType, s)));
            // connect to related sections: chapters and pages
            this.connectSections(statements, regex, subjects, doc);
        }
    }

    private void connectSections(List<Statement> statements, String regex, Set<String> subjects, Document doc) {
        log.info("Full text section query for: " + regex);
        List<Section> sections = this.documentService.sectionRegexQuery(doc.getId().toString(), regex);
        log.info("Found " + sections.size() + " chapters.");
        for (Section section : sections) {
            statements.addAll(this.localRepository.prepareResource(section.getIri(),
                    Integer.toString(section.getSerial()), this.vocabulary.section()));
            subjects.forEach(
                    s -> statements.add(this.localRepository.prepareRelation(section.getIri(), relationType, s)));
            // connect to related lines
            this.connectLines(statements, regex, subjects, doc, section.getSerial());
        }
        log.info("Full text page query for: " + regex);
        List<Section> pages = this.documentService.pageRegexQuery(doc.getId().toString(), regex);
        log.info("Found " + pages.size() + " pages.");
        for (Section page : pages) {
            statements.addAll(this.localRepository.prepareResource(page.getIri(), Integer.toString(page.getSerial()),
                    this.vocabulary.section()));
            subjects.forEach(s -> statements.add(this.localRepository.prepareRelation(page.getIri(), relationType, s)));
        }
    }

    private void connectLines(List<Statement> statements, String regex, Set<String> subjects, Document doc,
            int sectionNumber) {
        log.info("Full text lines query for: " + regex);
        List<Line> lines = this.documentService.lineRegexQuery(doc.getId().toString(), sectionNumber, regex);
        log.info("Found " + lines.size() + " lines.");
        for (Line line : lines) {
            statements.addAll(this.localRepository.prepareResource(line.getIri(), Integer.toString(line.getSerial()),
                    this.vocabulary.line()));
            subjects.forEach(s -> statements.add(this.localRepository.prepareRelation(line.getIri(), relationType, s)));
        }
    }

    protected void connectArticles(List<Statement> statements, List<String> phrases, String regex,
            Set<String> subjects) {
        log.info("Full text article query for: " + regex);
        Set<Article> articles = this.articleService.languageAgnosticFullTextQuery(phrases);
        log.info("Found " + articles.size() + " articles.");
        for (Article article : articles) {
            statements.addAll(this.localRepository.prepareResource(article.getIri(), article.getEntryWord(),
                    this.vocabulary.article()));
            subjects.forEach(
                    s -> statements.add(this.localRepository.prepareRelation(article.getIri(), relationType, s)));
        }
    }

    protected void connectNotes(List<Statement> statements, List<String> phrases, String regex, Set<String> subjects) {
        log.info("Full text note query for: " + regex);
        Set<Note> notes = this.noteService.languageAgnosticFullTextQuery(phrases);
        log.info("Found " + notes.size() + " notes.");
        for (Note note : notes) {
            statements.addAll(
                    this.localRepository.prepareResource(note.getIri(), note.getQuote(), this.vocabulary.note()));
            subjects.forEach(s -> statements.add(this.localRepository.prepareRelation(note.getIri(), relationType, s)));
        }
    }

    @SuppressWarnings("deprecation")
    @SneakyThrows(IOException.class)
    public String queryResources(MultipartFile file) {

        // prepare resource sets from file
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] resourceSets = content.split("\\R+");
        int missing = 0;
        List<EntityLinkCSVBean> links = Lists.newArrayList();
        log.info("Resource sets created. Number of sets: " + resourceSets.length);

        List<Statement> statements = Lists.newArrayList();

        for (String resourceList : resourceSets) {

            // dbpedia query for first resource literal from set
            List<String> resources = Lists.newArrayList(resourceList.split(",")).stream().distinct()
                    .collect(Collectors.toList());
            String firstResource = resources.get(0);
            log.info("Resource set (" + resources.size() + ") for: " + firstResource);
            log.info("Querying dbpedia.");
            List<Statement> dbPediaStatements = this.dbpediaRepository
                    .findStatements(DbpediaRepository.getBaseUri() + URLEncoder.encode(firstResource, "UTF-8"));
            log.info("Query returned " + dbPediaStatements.size() + " statements.");
            statements.addAll(dbPediaStatements);
            log.info("Current triple count: " + statements.size());

            if (dbPediaStatements.isEmpty()) {
                // not found in dbpedia
                ++missing;
                log.info("Missing resource in dbpedia: " + firstResource);
                links.add(EntityLinkCSVBean.builder().link("").entity(firstResource).build());
            } else {
                // gather sameAs subjects from result set
                Set<String> subjects = dbPediaStatements.stream().map(s -> s.getSubject().stringValue())
                        .collect(Collectors.toSet());
                log.info("Found " + subjects.size() + " subjects for resource.");

                // prepare regex for text search in db
                StringBuilder resourceRegexBuilder = new StringBuilder(firstResource);
                for (int i = 1; i < resources.size(); ++i) {
                    resourceRegexBuilder.append("|" + resources.get(i));
                }
                String resourceRegex = resourceRegexBuilder.toString();

                // connect to related documents
                this.connectDocuments(statements, resources, resourceRegex, subjects);

                // connect to related articles
                this.connectArticles(statements, resources, resourceRegex, subjects);

                // connect to related notes
                this.connectNotes(statements, resources, resourceRegex, subjects);

                // add link to response
                links.add(EntityLinkCSVBean.builder()
                        .link(DbpediaRepository.getBaseUri() + URLEncoder.encode(firstResource, "UTF-8"))
                        .entity(firstResource).build());
            }
        }

        // save statements to local db
        log.info("Saving locally. Number of statements: " + statements.size());
        localRepository.save(statements);

        // found resource stats
        log.info("All resources: " + resourceSets.length + ", missing: " + missing + ", percentage: "
                + (double) (resourceSets.length - missing) / (double) resourceSets.length * 100 + "%");

        // generate csv response
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
