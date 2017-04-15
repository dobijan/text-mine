package hu.bme.mit.textmine.mongo.document.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.mongo.corpus.service.CorpusService;
import hu.bme.mit.textmine.mongo.document.dal.DocumentRepository;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.DocumentFileDTO;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.rdf.TextMineVocabularyService;

@Service
public class DocumentService {
    
    private static Pattern pageNumberPattern = Pattern.compile("\\R(\\d+)\\R");

    @Autowired
    private DocumentRepository repository;
    
    @Autowired
    private TextMineVocabularyService vocabulary;

    @Autowired
    private CorpusService corpusService;
    
    public boolean exists(String id) {
        return this.repository.exists(id);
    }

    public List<Document> getAllDocuments() {
        return this.repository.findAll();
    }

    public Document getDocument(String id) {
        return this.repository.findOne(id);
    }

    public List<Document> getDocumentsByCorpus(String id) {
        return this.repository.findByCorpusId(new ObjectId(id));
    }

    public Document createDocument(DocumentFileDTO dto) throws IOException {
        Corpus corpus = this.corpusService.getCorpus(dto.getCorpusId());
        if (corpus == null) {
            return null;
        }
        String content = new String(dto.getFile().getBytes(), StandardCharsets.UTF_8);
        // remove chapter marks
        String preppedForPageDivision = content.replaceAll("\\[\\d+\\.\\]", "");
        //remove page marks
        String preppedForSectionDivision = content.replaceAll("\\R\\d+\\R", "");
        String[] pages = preppedForPageDivision.split("\\R+\\d+\\R+");
        String[] sections = preppedForSectionDivision.split("\\R+\\[\\d+\\.\\]\\R+");
        List<Section> sectionCollection = Lists.newArrayList();
        List<Section> pageCollection = Lists.newArrayList();
        // find number of first page
        int pageNumber = 1;
        Matcher pageNumberMatcher = pageNumberPattern.matcher(content);
        if (pageNumberMatcher.find()) {
            pageNumber = Integer.parseInt(pageNumberMatcher.group(1));
        }
        for (String page : pages) {
            List<String> lines = Arrays.asList(page.split("\\R+"));
            List<Line> numberedLines = IntStream.rangeClosed(1, lines.size())
                    .mapToObj(idx -> Line.builder().content(lines.get(idx - 1)).serial(idx).build())
                    .collect(Collectors.toList());
            pageCollection.add(Section.builder().content(page).lines(numberedLines).serial(pageNumber++).build());
        }
        int sectionNumber = 0;
        for (String section : sections) {
            List<String> lines = Arrays.asList(section.split("\\R+"));
            List<Line> numberedLines = IntStream.rangeClosed(1, lines.size())
                    .mapToObj(idx -> Line.builder().content(lines.get(idx - 1)).serial(idx).build())
                    .collect(Collectors.toList());
            sectionCollection.add(Section.builder().content(section).lines(numberedLines).serial(sectionNumber++).build());
        }
        Document document = Document.builder().author(dto.getAuthor()).content(content).corpus(corpus)
                .title(dto.getTitle()).sections(sectionCollection).pages(pageCollection).build();
        document.setIri(this.vocabulary.asResource(document));
        return this.repository.insert(document);
    }

    public Document updateDocument(Document document) {
        Document oldDocument = this.repository.findOne(document.getId().toString());
        if (oldDocument != null) {
            oldDocument.setAuthor(document.getAuthor());
            oldDocument.setContent(document.getContent());
            oldDocument.setCorpus(document.getCorpus());
            oldDocument.setTitle(document.getTitle());
            oldDocument.setSections(document.getSections());
            this.repository.save(oldDocument);
            return oldDocument;
        }
        return null;
    }

    public void removeDocument(Document document) {
        this.repository.delete(document);
    }

    public void removeDocuments(List<Document> documents) {
        this.repository.delete(documents);
    }
}
