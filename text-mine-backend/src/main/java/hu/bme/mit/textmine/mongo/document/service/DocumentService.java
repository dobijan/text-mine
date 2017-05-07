package hu.bme.mit.textmine.mongo.document.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.mongo.corpus.service.CorpusService;
import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.EntryExample;
import hu.bme.mit.textmine.mongo.dictionary.model.FormVariant;
import hu.bme.mit.textmine.mongo.dictionary.model.Inflection;
import hu.bme.mit.textmine.mongo.dictionary.model.Normalization;
import hu.bme.mit.textmine.mongo.dictionary.service.ArticleService;
import hu.bme.mit.textmine.mongo.document.dal.DocumentRepository;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.DocumentFileDTO;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
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

    public List<Line> lineRegexQuery(String documentId, int sectionNumber, String word) {
        return this.repository.getLinesByKeyword(documentId, sectionNumber, word);
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
        List<Article> articles = this.articleService.getArticlesByDocument(documentId);
        Map<Integer, List<Normalization>> pageNormalizations = Maps.newHashMap();
        for (Article article : articles) {
            for (FormVariant fv : article.getFormVariants()) {
                for (Inflection inflection : fv.getInflections()) {
                    for (EntryExample example : inflection.getExamples()) {
                        // tokenize example
                        List<String> exampleTokens = Lists
                                .newArrayList(this.cleanForNormalization(example.getExampleSentence()).split("\\s+"));
                        // find example tokens in text tokens
                        int exampleSentenceIndex = Collections.indexOfSubList(pageTokens.get(example.getPage()),
                                exampleTokens);
                        if (exampleSentenceIndex == -1) {
                            log.warn("Could not find example: " + example.getExampleSentence());
                            continue;
                        }
                        // find word index in example
                        List<String> inflectionTokens = Lists.newArrayList(inflection.getName().split("\\s+"));
                        int inflectionIndex = Collections.indexOfSubList(exampleTokens, inflectionTokens);
                        if (inflectionIndex == -1) {
                            log.warn("Could not find inflection in example: " + inflection.getName() + " -> "
                                    + example.getExampleSentence());
                            continue;
                        }
                        // compose normalization command object for later execution
                        Normalization normalization = Normalization.builder()
                                .startIndex(exampleSentenceIndex + inflectionIndex)
                                .endIndex(exampleSentenceIndex + inflectionIndex + inflectionTokens.size())
                                .replacement(article.getEntryWord()).build();
                        log.info("Found a normalization for page: " + example.getPage() + ", example: "
                                + example.getExampleSentence());
                        if (!pageNormalizations.containsKey(example.getPage())) {
                            pageNormalizations.put(example.getPage(), Lists.newArrayList(normalization));
                        } else {
                            pageNormalizations.get(example.getPage()).add(normalization);
                        }
                    }
                }
            }
        }
        // execute normalization commands
        Map<Integer, List<String>> normalizedPageTokens = Maps.newHashMap();
        for (Section page : doc.getPages()) {
            List<String> normalizedTokensForPage = Lists.newArrayList();
            List<Normalization> normalizationsForPage = pageNormalizations.get(page.getSerial());
            for (int i = 0; i < pageTokens.get(page.getSerial()).size(); ++i) {
                String token = pageTokens.get(page.getSerial()).get(i);
                // for each token check whether there is a normalization request pending for it
                // if there is more than one request for a token, it cannot be decided which replacement should occur
                final int j = i;
                List<Normalization> normalizationsForToken = normalizationsForPage.stream()
                        .filter(n -> (n.getStartIndex() <= j) && (n.getEndIndex() > j)).collect(Collectors.toList());
                if (normalizationsForToken.isEmpty()) {
                    // there are no normalizations for this token, output token unchanged
                    normalizedTokensForPage.add(token);
                } else if (normalizationsForToken.size() > 1) {
                    // conflicting normalizations, output token unchanged
                    log.error("Confliction normalizations! Token: " + token + ", normalizations: "
                            + normalizationsForToken.stream().map(Normalization::getReplacement)
                                    .collect(Collectors.joining(", ")));
                    normalizedTokensForPage.add(token);
                } else {
                    Normalization normalization = normalizationsForToken.get(0);
                    // perform normalization
                    // if this is the first token of the request, then output replacement, else output nothing
                    if (normalization.getStartIndex() == i) {
                        normalizedTokensForPage.add(normalization.getReplacement());
                    }
                }
            }
            normalizedPageTokens.put(page.getSerial(), normalizedTokensForPage);
        }
        // compose normalized page tokens into a normalized text
        List<String> tokens = Lists.newArrayList();
        normalizedPageTokens.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEachOrdered(e -> tokens.addAll(e.getValue()));
        doc.setNormalized(String.join(" ", tokens));
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
