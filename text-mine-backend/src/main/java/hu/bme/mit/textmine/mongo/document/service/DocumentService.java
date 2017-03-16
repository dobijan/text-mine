package hu.bme.mit.textmine.mongo.document.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.mongo.corpus.service.CorpusService;
import hu.bme.mit.textmine.mongo.document.dal.DocumentRepository;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.DocumentFileDTO;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.Section;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository repository;

    @Autowired
    private CorpusService corpusService;

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
        String[] pages = content.split("\\R+\\d+\\R+");
        List<Section> sections = new ArrayList<>();
        int sectionNumber = 0;
        for (String page : pages) {
            List<String> lines = Arrays.asList(page.split("\\R"));
            List<Line> numberedLines = IntStream.rangeClosed(1, lines.size())
                    .mapToObj(idx -> Line.builder().content(lines.get(idx - 1)).serial(idx).build())
                    .collect(Collectors.toList());
            sections.add(Section.builder().content(page).lines(numberedLines).serial(++sectionNumber).build());
        }
        Document document = Document.builder().author(dto.getAuthor()).content(content).corpus(corpus)
                .title(dto.getTitle()).sections(sections).build();
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
