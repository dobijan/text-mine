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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanToCsv;
import com.opencsv.bean.ColumnPositionMappingStrategy;

import hu.bme.mit.textmine.mongo.core.RdfEntity;
import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.service.ArticleService;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.QueryHits;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.mongo.note.service.NoteService;
import hu.bme.mit.textmine.rdf.dal.dbpedia.DbpediaRepository;
import hu.bme.mit.textmine.rdf.dal.local.LocalRdfRepository;
import hu.bme.mit.textmine.rdf.model.EntityLinkCSVBean;
import hu.bme.mit.textmine.rdf.model.Triple;
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

    @Getter
    protected String resourceType;

    protected abstract void setRelationType();

    protected abstract void setResourceType();

    protected abstract String getLabel(RdfEntity entity);

    protected abstract RdfEntity createResource(String resource);

    @PostConstruct
    public void init() {
        this.setRelationType();
        this.setResourceType();
    }

    @SneakyThrows(Exception.class)
    public boolean foundInDbpedia(String resource) {
        return !this.dbpediaRepository
                .findStatements(DbpediaRepository.getBaseUri() + URLEncoder.encode(resource, "UTF-8")).isEmpty();
    }

    protected void connectDocuments(List<Statement> statements, List<String> phrases, String regex, String subject) {
        log.info("Full text document query for: " + regex);
        QueryHits<Document> hits = this.documentService.languageAgnosticFullTextQuery(phrases, true);
        Set<Document> documents = Sets.union(hits.getBaseHits(), hits.getNoteHits());
        log.info("Found " + documents.size() + " documents.");
        for (Document doc : documents) {
            statements.addAll(
                    this.localRepository.prepareResource(doc.getIri(), doc.getTitle(), this.vocabulary.document(),
                            Lists.newArrayList()));
            statements.add(this.localRepository.prepareRelation(doc.getIri(), relationType, subject));
            // connect to related sections: chapters and pages
            this.connectSections(statements, regex, phrases, subject, doc.getId().toString());
        }
    }

    private void connectSections(List<Statement> statements, String regex, List<String> phrases, String subject,
            String docId) {
        log.info("Full text section query for: " + regex);
        QueryHits<Section> hits = this.documentService.sectionRegexQuery(docId, regex, phrases, true, true);
        Set<Section> sections = Sets.union(hits.getBaseHits(), hits.getNoteHits());
        log.info("Found " + sections.size() + " chapters.");
        for (Section section : sections) {
            statements.addAll(this.localRepository.prepareResource(section.getIri(),
                    Integer.toString(section.getSerial()), this.vocabulary.section(), Lists.newArrayList()));
            statements.add(this.localRepository.prepareRelation(section.getIri(), relationType, subject));
            // connect to related lines
            this.connectLines(statements, regex, phrases, subject, docId, section.getSerial());
        }
        log.info("Full text page query for: " + regex);
        hits = this.documentService.pageRegexQuery(docId, regex, phrases, true, true);
        Set<Section> pages = Sets.union(hits.getBaseHits(), hits.getNoteHits());
        log.info("Found " + pages.size() + " pages.");
        for (Section page : pages) {
            statements.addAll(this.localRepository.prepareResource(page.getIri(), Integer.toString(page.getSerial()),
                    this.vocabulary.section(), Lists.newArrayList()));
            statements.add(this.localRepository.prepareRelation(page.getIri(), relationType, subject));
        }
    }

    private void connectLines(List<Statement> statements, String regex, List<String> phrases, String subject,
            String docId, int serial) {
        log.info("Full text lines query for: " + regex);
        QueryHits<Line> hits = this.documentService.lineRegexQuery(docId, serial, regex,
                phrases, true);
        Set<Line> lines = Sets.union(hits.getBaseHits(), hits.getNoteHits());
        log.info("Found " + lines.size() + " lines.");
        for (Line line : lines) {
            statements.addAll(this.localRepository.prepareResource(line.getIri(), Integer.toString(line.getSerial()),
                    this.vocabulary.line(), Lists.newArrayList()));
            statements.add(this.localRepository.prepareRelation(line.getIri(), relationType, subject));
        }
    }

    protected void connectArticles(List<Statement> statements, List<String> phrases, String regex, String subject) {
        log.info("Full text article query for: " + regex);
        Set<Article> articles = this.articleService.languageAgnosticFullTextQuery(phrases, true);
        log.info("Found " + articles.size() + " articles.");
        for (Article article : articles) {
            statements.addAll(this.localRepository.prepareResource(article.getIri(), article.getEntryWord(),
                    this.vocabulary.article(), Lists.newArrayList()));
            statements.add(this.localRepository.prepareRelation(article.getIri(), relationType, subject));
        }
    }

    protected void connectNotes(List<Statement> statements, List<String> phrases, String regex, String subject) {
        log.info("Full text note query for: " + regex);
        Iterable<Note> notes = this.noteService.languageAgnosticFullTextQuery(phrases, true);
        int size = 0;
        for (Note note : notes) {
            statements.addAll(
                    this.localRepository.prepareResource(note.getIri(), note.getQuote(), this.vocabulary.note(),
                            Lists.newArrayList()));
            statements.add(this.localRepository.prepareRelation(note.getIri(), relationType, subject));
            ++size;
        }
        log.info("Found " + size + " notes.");
    }

    protected List<Line> linesForPredicates(String documentId, Integer sectionSerial, List<Triple> predicates,
            String varName) {
        if (predicates != null && !predicates.isEmpty()) {
            List<String> iris = this.localRepository.entitiesForPredicates(predicates, varName);
            return documentService.lineIriQuery(documentId, sectionSerial, iris);
        } else {
            return Lists.newArrayList();
        }
    }

    protected List<String> irisForLabel(String label) {
        return this.localRepository.getIriForResourceName(label, this.getResourceType());
    }

    protected List<Line> linesForIri(String documentId, Integer sectionSerial, List<String> iris) {
        List<String> lineIris = this.localRepository.entitiesForResource(iris, this.relationType,
                this.vocabulary.line());
        return documentService.lineIriQuery(documentId, sectionSerial, lineIris);
    }

    protected List<Section> sectionsForIri(String documentId, List<String> iris) {
        List<String> sectionIris = this.localRepository.entitiesForResource(Lists.newArrayList(iris), this.relationType,
                this.vocabulary.section());
        return documentService.sectionIriQuery(documentId, sectionIris);
    }

    protected List<Section> pagesForIri(String documentId, List<String> iris) {
        List<String> pageIris = this.localRepository.entitiesForResource(Lists.newArrayList(iris), this.relationType,
                this.vocabulary.section());
        return documentService.pageIriQuery(documentId, pageIris);
    }

    protected List<Note> notesForIri(String documentId, List<String> iris) {
        List<String> noteIris = this.localRepository.entitiesForResource(Lists.newArrayList(iris), this.relationType,
                this.vocabulary.note());
        return this.noteService.getNotesByIriAndDocumentId(documentId, noteIris);
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
            List<String> resources = Lists.newArrayList(resourceList.split(",")).stream()
                    .map(String::trim)
                    .distinct()
                    .filter(r -> r.length() > 0)
                    .collect(Collectors.toList());
            String firstResource = resources.get(0);
            log.info("Resource set (" + resources.size() + ") for: " + firstResource);
            log.info("Querying dbpedia.");
            Set<String> subjects = Sets.newHashSet();

            RdfEntity entity = this.createResource(firstResource);
            statements.addAll(this.localRepository.prepareResource(entity.getIri(), this.getLabel(entity),
                    this.getResourceType(), resources.subList(1, resources.size())));
            subjects.add(entity.getIri());

            try {
                List<Statement> dbPediaStatements = this.dbpediaRepository
                        .findStatements(DbpediaRepository.getBaseUri() + URLEncoder.encode(firstResource, "UTF-8"));
                log.info("Query returned " + dbPediaStatements.size() + " statements.");
                statements.addAll(dbPediaStatements);

                if (dbPediaStatements.isEmpty()) {
                    // not found in dbpedia
                    ++missing;
                    log.info("Missing resource in dbpedia: " + firstResource);
                    links.add(EntityLinkCSVBean.builder().link("").entity(firstResource).build());
                } else {
                    // gather sameAs subjects from result set
                    Set<String> dbPediaSubjects = dbPediaStatements.stream().map(s -> s.getSubject().stringValue())
                            .collect(Collectors.toSet());
                    statements.addAll(this.localRepository.createSameAsResources(entity.getIri(), dbPediaSubjects));
                    subjects.addAll(dbPediaSubjects);
                    log.info("Found " + dbPediaSubjects.size() + " dbpedia subjects for resource.");

                    // add link to response
                    links.add(EntityLinkCSVBean.builder()
                            .link(DbpediaRepository.getBaseUri() + URLEncoder.encode(firstResource, "UTF-8"))
                            .entity(firstResource).build());
                }
            } catch (Exception e) {
                log.warn(
                        "An error occurred during DBpedia data deserialization! This is a bug in the Virtuoso serializer. Omitting statements. Error: "
                                + e.getMessage());
            }

            log.info("Current triple count: " + statements.size());

            // prepare regex for text search in db
            StringBuilder resourceRegexBuilder = new StringBuilder(firstResource);
            for (int i = 1; i < resources.size(); ++i) {
                resourceRegexBuilder.append("|" + resources.get(i));
            }
            String resourceRegex = resourceRegexBuilder.toString();

            // connect to related documents
            this.connectDocuments(statements, resources, resourceRegex, entity.getIri());

            // connect to related articles
            this.connectArticles(statements, resources, resourceRegex, entity.getIri());

            // connect to related notes
            this.connectNotes(statements, resources, resourceRegex, entity.getIri());
        }

        // save statements to local db
        log.info("Saving locally. Number of statements: " + statements.size());
        int batchCounter = 0;
        int batchSize = 5000;
        log.info("Batch size: " + batchSize);
        while (batchCounter < statements.size()) {
            int upperIndex = Math.min(batchCounter + batchSize, statements.size());
            log.info("Saving from " + batchCounter + " to " + upperIndex);
            List<Statement> batch = statements.subList(batchCounter, upperIndex);
            localRepository.save(batch);
            batchCounter += batchSize;
        }

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
