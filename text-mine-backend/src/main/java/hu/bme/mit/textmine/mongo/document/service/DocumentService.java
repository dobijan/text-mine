package hu.bme.mit.textmine.mongo.document.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.mongo.corpus.service.CorpusService;
import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.FormVariant;
import hu.bme.mit.textmine.mongo.dictionary.model.Inflection;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.dictionary.service.ArticleService;
import hu.bme.mit.textmine.mongo.document.dal.DocumentRepository;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.DocumentFileDTO;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DocumentService {

    private static Pattern pageNumberPattern = Pattern.compile("\\R(\\d+)\\R");

    @Autowired
    private DocumentRepository repository;

    @Autowired
    private TextMineVocabularyService vocabulary;

    @Autowired
    private CorpusService corpusService;

    @Autowired
    private ArticleService articleService;

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

    public List<Document> getDocumentsByTitle(String title) {
        return this.repository.findByTitle(title);
    }

    public Set<Document> languageAgnosticFullTextQuery(List<String> phrases) {
        return this.repository.languageAgnosticQuery(phrases);
    }

    public List<Section> sectionRegexQuery(String documentId, String word) {
        return this.repository.getSectionsByKeyword(documentId, word);
    }

    public List<Section> pageRegexQuery(String documentId, String word) {
        return this.repository.getPagesByKeyword(documentId, word);
    }

    public Section getSectionBySerial(String documentId, Integer sectionNumber) {
        return this.repository.getPageBySerial(documentId, sectionNumber);
    }

    public Section getPageBySerial(String documentId, Integer sectionNumber) {
        return this.repository.getSectionBySerial(documentId, sectionNumber);
    }

    public List<Line> lineRegexQuery(String documentId, int sectionNumber, String word) {
        return this.repository.getLinesByKeyword(documentId, sectionNumber, word);
    }

    public List<Document> getDocumentsWithParams(String entryText, List<PartOfSpeech> partsOfSpeech,
            List<String> entryWords, List<String> documentIds, Integer offset, Integer limit) {
        return this.repository.queryWithParams(entryText, partsOfSpeech, entryWords, documentIds, offset, limit);
    }

    public Document normalizeDocument(String documentId) {
        if (!this.repository.exists(documentId)) {
            return null;
        }
        Document doc = this.getDocument(documentId);
        // tokenize input text
        Map<Integer, List<String>> pageTokens = Maps.newHashMap();
        for (Section page : doc.getPages()) {
            // clean page content (apparently this is required)
            pageTokens.put(page.getSerial(),
                    Lists.newArrayList(this.cleanForNormalization(page.getContent()).split("\\s+")));
        }
        StringBuffer textTokens = new StringBuffer(String.join(" ", Iterables.concat(pageTokens.values())));
        List<Article> articles = this.articleService.getArticlesByDocument(documentId);
        Map<String, String> replacements = Maps.newTreeMap((left, right) -> {
            int order = Integer.compare(right.length(), left.length());
            if (order == 0) {
                return right.compareTo(left);
            }
            return order;
        });
        for (Article article : articles) {
            for (FormVariant fv : article.getFormVariants()) {
                for (Inflection inflection : fv.getInflections()) {
                    if (replacements.containsKey(inflection.getName())) {
                        log.warn("Multiple idetical inflections found for: " + inflection.getName());
                    } else {
                        replacements.put(inflection.getName(), article.getEntryWord());
                    }
                }
            }
        }
        for (val entry : replacements.entrySet()) {
            // find inflection in text
            // Matcher m = Pattern.compile(Pattern.quote(entry.getKey())).matcher(textTokens);
            boolean found = false;
            int lastIndex = 0;
            int idx = textTokens.indexOf(entry.getKey(), lastIndex);
            while (idx != -1) {
                found = true;
                textTokens = textTokens.replace(idx, idx + entry.getKey().length(), entry.getValue());
                log.info("Found a normalization for inflection: " + entry.getKey() + ", index: " + idx);
                lastIndex = idx + entry.getValue().length();
                idx = textTokens.indexOf(entry.getKey(), lastIndex);
            }
            if (!found) {
                log.warn("Could not find inflection: " + entry.getKey());
                continue;
            }
        }
        // compose normalized page tokens into a normalized text
        doc.setNormalized(textTokens.toString());
        return doc;
    }

    public Document createDocument(DocumentFileDTO dto) throws IOException {
        Corpus corpus = this.corpusService.getCorpus(dto.getCorpusId());
        if (corpus == null) {
            return null;
        }
        String content = new String(dto.getFile().getBytes(), StandardCharsets.UTF_8);
        // remove chapter marks
        String preppedForPageDivision = content.replaceAll("\\[\\d+\\.\\]", "");
        // remove page marks
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
            List<Line> numberedLines = IntStream.rangeClosed(1, lines.size()).mapToObj(idx -> {
                Line line = Line.builder().content(lines.get(idx - 1)).serial(idx).build();
                line.setIri(this.vocabulary.asResource(line));
                return line;
            }).collect(Collectors.toList());
            Section pageSection = Section.builder().content(page).lines(numberedLines).serial(pageNumber++).build();
            pageSection.setIri(this.vocabulary.asResource(pageSection));
            pageCollection.add(pageSection);
        }
        int sectionNumber = 0;
        for (String section : sections) {
            List<String> lines = Arrays.asList(section.split("\\R+"));
            List<Line> numberedLines = IntStream.rangeClosed(1, lines.size()).mapToObj(idx -> {
                Line line = Line.builder().content(lines.get(idx - 1)).serial(idx).build();
                line.setIri(this.vocabulary.asResource(line));
                return line;
            }).collect(Collectors.toList());
            Section sectionObj = Section.builder().content(section).lines(numberedLines).serial(sectionNumber++)
                    .build();
            sectionObj.setIri(this.vocabulary.asResource(sectionObj));
            sectionCollection.add(sectionObj);
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

    private String cleanForNormalization(String input) {
        // clean some kind of references
        String cleaned = input.replaceAll("\\s*\\[.+\\]\\s*", "");
        // clean hypehenation
        cleaned = cleaned.replaceAll("(\\S+)-\\n(\\S+)", "$1$2\n");
        // clean number appended to words
        cleaned = cleaned.replaceAll("\\s+([^\\d\\s]+)\\d+", " $1 ");
        // clean punctuation
        cleaned = cleaned.replaceAll("[.,;?!:]", "");
        return cleaned;
    }
}
