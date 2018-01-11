package hu.bme.mit.textmine.mongo.dictionary.service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;

import hu.bme.mit.textmine.mongo.core.SearchStrategy;
import hu.bme.mit.textmine.mongo.dictionary.dal.ArticleRepository;
import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.ArticleFileDTO;
import hu.bme.mit.textmine.mongo.dictionary.model.DocumentArticles;
import hu.bme.mit.textmine.mongo.dictionary.model.EntryExample;
import hu.bme.mit.textmine.mongo.dictionary.model.FormVariant;
import hu.bme.mit.textmine.mongo.dictionary.model.Inflection;
import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeechCsvBean;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import hu.bme.mit.textmine.solr.dal.article.SolrArticleRepository;
import hu.bme.mit.textmine.solr.dal.document.SolrDocumentRepository;
import hu.bme.mit.textmine.solr.dal.form_variant.SolrFormVariantRepository;
import hu.bme.mit.textmine.solr.dal.inflection.SolrInflectionRepository;
import hu.bme.mit.textmine.solr.dal.page.SolrPageRepository;
import hu.bme.mit.textmine.solr.dal.section.SolrSectionRepository;
import hu.bme.mit.textmine.solr.model.SolrArticle;
import hu.bme.mit.textmine.solr.model.SolrArticleWrapper;
import hu.bme.mit.textmine.solr.model.SolrDocument;
import hu.bme.mit.textmine.solr.model.SolrFormVariant;
import hu.bme.mit.textmine.solr.model.SolrInflection;
import hu.bme.mit.textmine.solr.model.SolrPage;
import hu.bme.mit.textmine.solr.model.SolrSection;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ArticleService {

    private static final Pattern entryWordPattern = Pattern.compile("^.*<U>(.*)</U>.*$"),
            internalReferencPattern = Pattern.compile("^.*<R>(.*)</R>.*$"),
            externalReferencPattern = Pattern.compile("^.*<RT>(.*)</RT>.*$"),
            properNounPattern = Pattern.compile("^.*<T_/>.*$"), derivativePattern = Pattern.compile("^.*<KT_/>.*$"),
            editorNotePattern = Pattern.compile("^.*<K>(.*)</K>.*$"),
            meaningPattern = Pattern.compile("^.*<J>(.*)</J>.*$"),
            formVariantPattern = Pattern.compile("^.*<Q>(.*)</Q>.*$"),
            inflectionPattern = Pattern.compile("^.*<B>(.*)</B>( – )?(\\d+)?.*$"),
            occurrencePattern = Pattern.compile("^(.*<I>.*</I>.*) \\(TL (\\d+)\\).*$"),
            entryPattern = Pattern.compile("\\<entry headword=\"(.*)\"\\>\\R+([\\S\\s]*?)\\R+\\<\\/entry\\>");

    @Value("${search.strategy}")
    private SearchStrategy searchStrategy;

    @Autowired
    @Lazy
    private ArticleRepository repository;

    @Autowired
    @Lazy
    private DocumentService documentService;

    @Autowired
    private TextMineVocabularyService vocabulary;

    @Autowired
    private SolrArticleRepository solrArticleRepository;

    @Autowired
    private SolrFormVariantRepository solrFormVariantRepository;

    @Autowired
    private SolrInflectionRepository solrInflectionRepository;

    @Autowired
    private SolrDocumentRepository solrDocumentRepository;

    @Autowired
    private SolrPageRepository solrPageRepository;

    @Autowired
    private SolrSectionRepository solrSectionRepository;

    // @Autowired
    // private SolrLineRepository solrLineRepository;
    //
    // @Autowired
    // private SolrWordRepository solrWordRepository;

    public boolean exists(String id) {
        return this.repository.exists(id);
    }

    public Article getArticle(String id) {
        return this.repository.findById(id).orElse(null);
    }

    public List<Article> getAll() {
        return this.repository.findAll();
    }

    public List<Article> getArticlesByDocument(String id) {
        if (!this.documentService.exists(id)) {
            return null;
        }
        return this.repository.findByDocumentId(new ObjectId(id));
    }

    public List<Article> getArticlesByEntryWord(String entryWord) {
        return this.repository.findByEntryWord(entryWord);
    }

    public List<Article> getArticlesByFormVariant(String formVariant) {
        return this.repository.findByFormVariant(formVariant);
    }

    public List<Article> getArticlesByInflection(String inflection) {
        return this.repository.findByInflection(inflection);
    }

    public Set<Article> languageAgnosticFullTextQuery(List<String> phrases, boolean exact) {
        if (SearchStrategy.MONGO.equals(this.searchStrategy)) {
            return this.repository.languageAgnosticQuery(phrases);
        } else {
            List<SolrArticle> solrArticles = this.solrArticleRepository.phrasesQuery(null, phrases, exact, true);
            return Sets.newHashSet(this.repository.findAllById(
                    solrArticles.stream().map(SolrArticle::getId).map(ObjectId::new).collect(Collectors.toList())));
        }
    }

    public List<Article> getArticlesByPartsOfSpeech(List<PartOfSpeech> pos) {
        return this.repository.findAllByPartsOfSpeech(pos);
    }

    public List<Article> queryWithParams(String entryWord, String formVariant, String inflection,
            List<PartOfSpeech> partOfSpeech, List<String> documentIds, String corpusId,
            MatchingStrategy matchingStrategy, Integer offset, Integer limit) {
        if (SearchStrategy.MONGO.equals(this.searchStrategy)) {
            List<Document> docs = this.documentService.getDocumentsByCorpus(corpusId);
            documentIds = Lists.newArrayList(Sets.intersection(Sets.newHashSet(documentIds),
                    docs.stream().map(Document::getId).collect(Collectors.toSet())));
            return this.repository.findwithParams(entryWord, formVariant, inflection, partOfSpeech, documentIds,
                    matchingStrategy, offset, limit);
        } else {
            List<SolrArticle> articles = this.solrArticleRepository.findWithParams(entryWord, formVariant, inflection,
                    partOfSpeech, documentIds, corpusId, matchingStrategy, offset, limit);
            List<String> articleIds = articles.stream().map(SolrArticle::getId).collect(Collectors.toList());
            List<Article> mongoArticles = Lists.newArrayList();
            this.repository.findAllById(articleIds).forEach(mongoArticles::add);
            return mongoArticles;
        }
    }

    public List<Article> frequentArticlesByPOS(PartOfSpeech pos, List<String> documentIds, int page, int size) {
        List<SolrArticle> solrArticles = this.solrArticleRepository.findByDocumentIdAndPOSOrderByFrequency(pos,
                documentIds, page, size);
        return this.repository
                .findAllById(solrArticles.stream().map(a -> new ObjectId(a.getId())).collect(Collectors.toList()));
    }

    public List<Article> findInDocumentSection(String documentId, Integer sectionNumber) {
        if (SearchStrategy.MONGO.equals(this.searchStrategy)) {
            return this.repository.findByDocumentSection(documentId, sectionNumber);
        } else {
            Section section = this.documentService.getSectionBySerial(documentId, sectionNumber);
            List<ObjectId> ids = this.solrInflectionRepository.findInText(section.getContent()).stream()
                    .map(id -> new ObjectId(id)).collect(Collectors.toList());
            return this.repository.findAllById(ids);
        }
    }

    public List<Article> findInDocumentPage(String documentId, Integer pageNumber) {
        if (SearchStrategy.MONGO.equals(this.searchStrategy)) {
            return this.repository.findByDocumentPage(documentId, pageNumber);
        } else {
            Section page = this.documentService.getPageBySerial(documentId, pageNumber);
            List<ObjectId> ids = this.solrInflectionRepository.findInText(page.getContent()).stream()
                    .map(id -> new ObjectId(id)).collect(Collectors.toList());
            return this.repository.findAllById(ids);
        }
    }

    public List<DocumentArticles> findByDocumentWithParams(String entryWord, String formVariant, String inflection,
            List<PartOfSpeech> partOfSpeech, MatchingStrategy matchingStrategy, Integer posCount) {
        if (SearchStrategy.MONGO.equals(this.searchStrategy)) {
            return this.repository.getArticlesByDocumentWithParams(entryWord, formVariant, inflection, partOfSpeech,
                    matchingStrategy, posCount);
        } else {
            GroupResult<SolrArticle> groups = this.solrArticleRepository.findWithParamsByDocumentId(entryWord,
                    formVariant, inflection, partOfSpeech, matchingStrategy, posCount);
            List<DocumentArticles> docArticles = Lists.newArrayList();
            groups.getGroupEntries().forEach(entry -> {
                List<Article> articles = this.repository.findAllById(entry.getResult().getContent().stream()
                        .map(a -> new ObjectId(a.getId())).collect(Collectors.toList()));
                docArticles.add(DocumentArticles.builder()
                        .document(entry.getGroupValue())
                        .articles(articles)
                        .build());
            });
            return docArticles;
        }
    }

    public List<PartOfSpeechCsvBean> attachPOSInfo(ArticleFileDTO dto) throws IOException {
        if (!this.documentService.exists(dto.getDocumentId())) {
            log.warn("No such document exists!");
            return null;
        }
        String content = new String(dto.getFile().getBytes(), StandardCharsets.UTF_8);
        List<PartOfSpeechCsvBean> posTags = this.parseCsvToPos(content);
        // replace all control characters with nothing
        for (PartOfSpeechCsvBean postag : posTags) {
            postag.setWord(postag.getWord().replaceAll("\\p{C}", ""));
            postag.setPartOfSpeech(postag.getPartOfSpeech().replaceAll("\\p{C}", ""));
        }
        List<String> foundWords = Lists.newArrayList();
        for (PartOfSpeechCsvBean posTag : posTags) {
            if (!this.repository.updatePOS(dto.getDocumentId(), posTag.getWord().trim(),
                    Lists.newArrayList(posTag.getPartOfSpeechMapped()))) {
                log.error("Could not find article for: " + posTag.getWord().trim());
            } else {
                String word = posTag.getWord().trim();
                log.info("Found article for: " + word);
                foundWords.add(word);
            }
        }
        Map<String, Article> articleMap = Maps.newHashMap();
        List<Article> as = this.repository.findByDocumentId(new ObjectId(dto.getDocumentId()));
        for (Article a : as) {
            articleMap.put(a.getEntryWord(), a);
        }
        for (String word : foundWords) {
            log.info("Updating Solr entities for: " + word);
            // this.removeSolrEntitiesByArticle(a.getId().toString());
            this.saveSolrEntities(this.createSolrEntities(articleMap.get(word), dto.getDocumentId()));
        }
        log.info("Committing.");
        this.commitSolr();
        log.info("Get document solr entities.");
        List<Article> articles = this.repository.findByDocumentId(new ObjectId(dto.getDocumentId()));
        SolrDocument solrDocument = this.solrDocumentRepository.findById(dto.getDocumentId()).orElse(null);
        List<SolrPage> solrPages = this.solrPageRepository.findByDocumentId(dto.getDocumentId());
        List<SolrSection> solrSections = this.solrSectionRepository.findByDocumentId(dto.getDocumentId());
        // List<SolrLine> solrLines = this.solrLineRepository.findByDocumentId(dto.getDocumentId());
        // List<SolrWord> solrWords = this.solrWordRepository.findByDocumentId(dto.getDocumentId());
        // tokenize input text
        log.info("Part of speech tagging.");
        Map<String, Set<PartOfSpeech>> tags = this.createPosMap(articles);
        solrSections.forEach(s -> s
                .setPartsOfSpeech(this.textToPosTags(documentService.cleanForNormalization(s.getContent()), tags)));
        log.info("Sections done");
        solrPages.forEach(p -> p
                .setPartsOfSpeech(this.textToPosTags(documentService.cleanForNormalization(p.getContent()), tags)));
        log.info("Pages done.");
        solrDocument
                .setPartsOfSpeech(solrPages.stream().map(SolrPage::getPartsOfSpeech).collect(Collectors.joining("\n")));
        log.info("Document done. Saving.");
        this.solrDocumentRepository.saveNoCommit(solrDocument);
        this.solrSectionRepository.saveNoCommit(solrSections);
        this.solrPageRepository.saveNoCommit(solrPages);
        log.info("Committing.");
        this.commitSolr();
        return posTags;
    }

    private Map<String, Set<PartOfSpeech>> createPosMap(List<Article> documentArticles) {
        Map<String, Set<PartOfSpeech>> tags = Maps.newHashMap();
        for (Article article : documentArticles) {
            if (article.getPartOfSpeech() == null || article.getPartOfSpeech().isEmpty()
                    || article.getPartOfSpeech().stream().allMatch(Objects::isNull)) {
                continue;
            }
            for (FormVariant formVariant : article.getFormVariants()) {
                for (Inflection inflection : formVariant.getInflections()) {
                    if (inflection.getExamples() == null || inflection.getExamples().isEmpty()) {
                        continue;
                    }
                    if (tags.containsKey(inflection.getName())) {
                        tags.get(inflection.getName()).addAll(article.getPartOfSpeech());
                    } else {
                        tags.put(inflection.getName(), Sets.newHashSet(article.getPartOfSpeech()));
                    }
                }
            }
        }
        return tags;
    }

    private String textToPosTags(String text, Map<String, Set<PartOfSpeech>> tags) {
        String replacedText = new String(text);
        for (String key : tags.keySet()) {
            replacedText = replacedText.replaceAll("([\\s\\p{Z}]+)(" + Pattern.quote(key) + ")([\\s\\p{Z}]+)",
                    "$1" + Matcher.quoteReplacement(key) + "["
                            + Matcher.quoteReplacement(String.join("][",
                                    tags.get(key).stream().filter(Objects::nonNull).map(PartOfSpeech::toString)
                                            .collect(Collectors.toList())))
                            + "]" + "$3");
        }
        return replacedText;
    }

    public List<Article> createMultipleArticles(ArticleFileDTO dto) throws IOException {
        if (!this.documentService.exists(dto.getDocumentId())) {
            log.warn("No such document exists!");
            return null;
        }
        Document document = this.documentService.getDocument(dto.getDocumentId());
        String content = new String(dto.getFile().getBytes(), StandardCharsets.UTF_8);
        Matcher entryMatcher = entryPattern.matcher(content);
        List<Article> articles = Lists.newArrayList();
        Map<String, String> entryStrings = Maps.newHashMap();
        while (entryMatcher.find()) {
            entryStrings.put(entryMatcher.group(1), entryMatcher.group(2));
        }
        log.info("Articles found: " + entryStrings.size());
        int idx = 1;
        for (String section : entryStrings.values()) {
            log.info("Parsing " + idx + ". article.");
            ++idx;
            Article article = this.parseToArticle(section, document);
            if (article != null) {
                articles.add(article);
            }
        }
        this.repository.insert(articles);
        articles.forEach(a -> this.saveSolrEntities(this.createSolrEntities(a, dto.getDocumentId())));
        this.commitSolr();
        this.documentService.normalizeDocument(document, articles);
        return articles;
    }

    public Article createArticle(ArticleFileDTO dto) throws IOException {
        if (!this.documentService.exists(dto.getDocumentId())) {
            log.warn("No such document exists!");
            return null;
        }
        Document document = this.documentService.getDocument(dto.getDocumentId());
        String content = new String(dto.getFile().getBytes(), StandardCharsets.UTF_8);
        Article article = this.parseToArticle(content, document);
        if (article == null) {
            return null;
        }
        article = this.repository.insert(article);
        this.saveSolrEntities(this.createSolrEntities(article, dto.getDocumentId()));
        this.commitSolr();
        return article;
    }

    private SolrArticleWrapper createSolrEntities(final Article article, String documentId) {
        SolrArticle solrArticle = SolrArticle.builder()
                .derivative(article.getDerivative())
                .documentId(documentId)
                .editorNote(article.getEditorNote())
                .entryWord(article.getEntryWord())
                .externalReferences(article.getExternalReferences())
                .formVariants(article.getFormVariants().stream().map(v -> v.getName()).collect(Collectors.toList()))
                .id(article.getId().toString())
                .inflections(article.getFormVariants().stream()
                        .map(v -> v.getInflections().stream().map(i -> i.getName()).collect(Collectors.toList()))
                        .flatMap(l -> l.stream()).collect(Collectors.toList()))
                .internalReferences(article.getInternalReferences())
                .meaning(article.getMeaning())
                .partOfSpeech(
                        article.getPartOfSpeech().stream()
                                .filter(Objects::nonNull)
                                .map(pos -> pos.toString())
                                .collect(Collectors.toList()))
                .properNoun(article.getProperNoun())
                .build();
        List<SolrFormVariant> solrFormVariants = Lists.newArrayList();
        List<SolrInflection> solrInflections = Lists.newArrayList();
        article.getFormVariants().forEach(v -> {
            v.getInflections().forEach(i -> {
                List<String> examples = i.getExamples().stream().map(e -> e.getExampleSentence())
                        .collect(Collectors.toList());
                solrInflections.add(SolrInflection.builder()
                        .articleId(article.getId().toString())
                        .documentId(documentId)
                        .examples(examples)
                        .id(i.getName() + "#" + v.getName() + "@" + article.getId().toString())
                        .name(i.getName())
                        .occurrences(i.getOccurrences())
                        .build());
                solrArticle.setFrequency(solrArticle.getFrequency() + examples.size());
            });
            solrFormVariants.add(SolrFormVariant.builder()
                    .articleId(article.getId().toString())
                    .documentId(documentId)
                    .id(v.getName() + "@" + article.getId().toString())
                    .inflections(v.getInflections().stream().map(i -> i.getName()).collect(Collectors.toList()))
                    .name(v.getName())
                    .build());
        });
        return SolrArticleWrapper.builder()
                .article(solrArticle)
                .formVariants(solrFormVariants)
                .inflections(solrInflections)
                .build();
    }

    private void saveSolrEntities(SolrArticleWrapper entities) {
        SolrArticle a = entities.getArticle();
        this.solrArticleRepository.saveNoCommit(a);
        List<SolrFormVariant> fws = entities.getFormVariants();
        if (!fws.isEmpty()) {
            this.solrFormVariantRepository.saveNoCommit(fws);
        }
        List<SolrInflection> ifs = entities.getInflections();
        if (!ifs.isEmpty()) {
            this.solrInflectionRepository.saveNoCommit(ifs);
        }
    }

    private void commitSolr() {
        this.solrArticleRepository.commit();
        this.solrFormVariantRepository.commit();
        this.solrInflectionRepository.commit();
        this.solrDocumentRepository.commit();
        this.solrPageRepository.commit();
        this.solrSectionRepository.commit();
    }

    public void removeSolrEntitiesByDocument(String documentId) {
        List<SolrArticle> solrArticles = this.solrArticleRepository.findByDocumentId(documentId);
        List<SolrInflection> solrInflections = this.solrInflectionRepository.findByDocumentId(documentId);
        List<SolrFormVariant> solrFormVariants = this.solrFormVariantRepository.findByDocumentId(documentId);
        if (!solrArticles.isEmpty()) {
            this.solrArticleRepository.deleteAll(solrArticles);
        }
        if (!solrFormVariants.isEmpty()) {
            this.solrFormVariantRepository.deleteAll(solrFormVariants);
        }
        if (!solrInflections.isEmpty()) {
            this.solrInflectionRepository.deleteAll(solrInflections);
        }
    }

    private void removeSolrEntitiesByArticle(String articleId) {
        List<SolrFormVariant> solrFormVariants = this.solrFormVariantRepository.findByArticleId(articleId);
        List<SolrInflection> solrInflections = this.solrInflectionRepository.findByArticleId(articleId);
        this.solrArticleRepository.deleteById(articleId);
        if (!solrFormVariants.isEmpty()) {
            this.solrFormVariantRepository.deleteAll(solrFormVariants);
        }
        if (!solrInflections.isEmpty()) {
            this.solrInflectionRepository.deleteAll(solrInflections);
        }
    }

    @SuppressWarnings("deprecation")
    private List<PartOfSpeechCsvBean> parseCsvToPos(String content) {
        CsvToBean<PartOfSpeechCsvBean> processor = new CsvToBean<>();
        ColumnPositionMappingStrategy<PartOfSpeechCsvBean> mappingStrategy = new ColumnPositionMappingStrategy<>();
        mappingStrategy.setColumnMapping(new String[] {
                "word", "partOfSpeech"
        });
        mappingStrategy.setType(PartOfSpeechCsvBean.class);
        List<PartOfSpeechCsvBean> beans = processor.parse(mappingStrategy,
                new CSVReader(new StringReader(content), ';'));
        return beans;
    }

    private Article parseToArticle(String input, Document document) {
        List<String> lines = Arrays.asList(input.split("\\R+"));
        Article article = new Article();
        article.setDocumentId(document.getId().toString());
        article.setProperNoun(false);
        article.setDerivative(false);
        article.setInternalReferences(Lists.newArrayList());
        article.setExternalReferences(Lists.newArrayList());
        article.setFormVariants(Lists.newArrayList());
        article.setPartOfSpeech(Lists.newArrayList());

        FormVariant currentFormVariant = null;
        Inflection currentInflection = null;

        for (String line : lines) {
            // find article entry word
            Matcher m = entryWordPattern.matcher(line);
            if (m.matches()) {
                log.info("Constructing article for: " + m.group(1));
                article.setEntryWord(m.group(1));
                continue;
            }
            // find if proper noun
            m = properNounPattern.matcher(line);
            if (m.matches()) {
                article.setProperNoun(true);
                continue;
            }
            // find if derivative
            m = derivativePattern.matcher(line);
            if (m.matches()) {
                article.setDerivative(true);
                continue;
            }
            // find a reference
            m = internalReferencPattern.matcher(line);
            if (m.matches()) {
                article.getInternalReferences().add(m.group(1));
                continue;
            }
            // find an external reference
            m = externalReferencPattern.matcher(line);
            if (m.matches()) {
                article.getExternalReferences().add(m.group(1));
                continue;
            }
            // find editor note
            m = editorNotePattern.matcher(line);
            if (m.matches()) {
                article.setEditorNote(m.group(1));
                continue;
            }
            // find meaning (of life)
            m = meaningPattern.matcher(line);
            if (m.matches()) {
                article.setMeaning(m.group(1));
                continue;
            }
            // find a form variant header
            m = formVariantPattern.matcher(line);
            if (m.matches()) {
                FormVariant formVariant = new FormVariant();
                formVariant.setName(m.group(1));
                formVariant.setInflections(Lists.newArrayList());
                article.getFormVariants().add(formVariant);
                currentFormVariant = formVariant;
                continue;
            }
            // find an inflection
            m = inflectionPattern.matcher(line);
            if (m.matches()) {
                Inflection inflection = new Inflection();
                inflection.setName(m.group(1));
                String occurrences = m.group(3);
                if (occurrences != null) {
                    inflection.setOccurrences(Integer.parseInt(occurrences));
                }
                inflection.setExamples(Lists.newArrayList());
                currentInflection = inflection;
                if (currentFormVariant == null) {
                    FormVariant formVariant = new FormVariant();
                    formVariant.setName(m.group(1));
                    formVariant.setInflections(Lists.newArrayList());
                    article.getFormVariants().add(formVariant);
                    currentFormVariant = formVariant;
                }
                currentFormVariant.getInflections().add(inflection);
            }
            // find an occurrence
            m = occurrencePattern.matcher(line);
            if (m.matches()) {
                String exampleText = m.group(1).replace("<I>", "").replace("</I>", "");
                EntryExample example = new EntryExample();
                example.setExampleSentence(exampleText);
                example.setPage(Integer.parseInt(m.group(2)));
                if (currentInflection == null) {
                    Inflection inflection = new Inflection();
                    inflection.setName(m.group(1));
                    inflection.setExamples(Lists.newArrayList());
                    currentInflection = inflection;
                    currentFormVariant.getInflections().add(inflection);
                }
                currentInflection.getExamples().add(example);
            }
        }
        if (article.getEntryWord() == null) {
            return null;
        }
        article.setIri(this.vocabulary.asResource(article));
        return article;
    }

    public Article updateArticle(Article article) {
        article = this.repository.save(article);
        this.removeSolrEntitiesByArticle(article.getId().toString());
        this.saveSolrEntities(this.createSolrEntities(article, article.getDocumentId().toString()));
        return article;
    }

    public void removeArticle(String id) {
        this.repository.deleteById(id);
        this.removeSolrEntitiesByArticle(id);
    }

    public void removeArticles(List<Article> articles) {
        this.repository.deleteAll(articles);
        articles.forEach(a -> this.removeSolrEntitiesByArticle(a.getId().toString()));
    }
}
