package hu.bme.mit.textmine.mongo.document.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import hu.bme.mit.textmine.mongo.core.SearchStrategy;
import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.mongo.corpus.service.CorpusService;
import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.FormVariant;
import hu.bme.mit.textmine.mongo.dictionary.model.Inflection;
import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.document.dal.DocumentRepository;
import hu.bme.mit.textmine.mongo.document.model.AttachmentDTO;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.DocumentFileDTO;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.QueryHits;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.mongo.note.service.NoteService;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import hu.bme.mit.textmine.solr.dal.SolrWordRepository;
import hu.bme.mit.textmine.solr.dal.article.SolrArticleRepository;
import hu.bme.mit.textmine.solr.dal.document.SolrDocumentRepository;
import hu.bme.mit.textmine.solr.dal.line.SolrLineRepository;
import hu.bme.mit.textmine.solr.dal.note.SolrNoteRepository;
import hu.bme.mit.textmine.solr.dal.page.SolrPageRepository;
import hu.bme.mit.textmine.solr.dal.section.SolrSectionRepository;
import hu.bme.mit.textmine.solr.model.PartOfSpeechStatistics;
import hu.bme.mit.textmine.solr.model.SolrDocument;
import hu.bme.mit.textmine.solr.model.SolrDocumentWrapper;
import hu.bme.mit.textmine.solr.model.SolrLine;
import hu.bme.mit.textmine.solr.model.SolrNote;
import hu.bme.mit.textmine.solr.model.SolrPage;
import hu.bme.mit.textmine.solr.model.SolrSection;
import hu.bme.mit.textmine.solr.model.SolrWord;
import hu.bme.mit.textmine.solr.model.WordTerm;

@Service
@Lazy
public class DocumentService {

    private static Pattern pageNumberPattern = Pattern.compile("\\R(\\d+)\\R");

    @Value("${search.strategy}")
    private SearchStrategy searchStrategy;

    @Autowired
    @Lazy
    private DocumentRepository repository;

    @Autowired
    private NoteService noteService;

    @Autowired
    private TextMineVocabularyService vocabulary;

    @Autowired
    private CorpusService corpusService;

    @Autowired
    private SolrDocumentRepository solrDocumentRepository;

    @Autowired
    private SolrNoteRepository solrNoteRepository;

    @Autowired
    private SolrPageRepository solrPageRepository;

    @Autowired
    private SolrSectionRepository solrSectionRepository;

    @Autowired
    private SolrLineRepository solrLineRepository;

    @Autowired
    private SolrWordRepository solrWordRepository;

    @Autowired
    private SolrArticleRepository solrArticleRepository;

    public boolean exists(String id) {
        return this.repository.exists(id);
    }

    public List<Document> getAllDocuments() {
        return this.repository.findAll();
    }

    public Document getDocument(String id) {
        return this.repository.findById(id).orElse(null);
    }

    public List<Document> getDocumentsByCorpus(String id) {
        return this.repository.findByCorpusId(new ObjectId(id));
    }

    public List<Document> getDocumentsByTitle(String title) {
        return this.repository.findByTitle(title);
    }

    public QueryHits<Document> languageAgnosticFullTextQuery(List<String> phrases, boolean exact) {
        if (SearchStrategy.MONGO.equals(this.searchStrategy)) {
            Iterable<Note> notes = this.noteService.languageAgnosticFullTextQuery(phrases, exact);
            Set<Document> docs = this.repository.findAllByIds(StreamSupport.stream(notes.spliterator(), false)
                    .map(n -> n.getDocumentId()).distinct().collect(Collectors.toList()));
            return QueryHits.<Document>builder()
                    .baseHits(this.repository.languageAgnosticQuery(phrases))
                    .noteHits(docs)
                    .build();
        } else {
            List<SolrNote> solrNotes = this.solrNoteRepository.phraseQuery(Optional.empty(), Optional.empty(), phrases,
                    exact, true);
            List<SolrDocument> solrDocuments = this.solrDocumentRepository.phrasesQuery(phrases, exact, true);
            return QueryHits.<Document>builder()
                    .baseHits(this.repository
                            .findAllByIds(solrDocuments.stream().map(SolrDocument::getId).collect(Collectors.toList())))
                    .noteHits(this.repository
                            .findAllByIds(solrNotes.stream().map(SolrNote::getId).collect(Collectors.toList())))
                    .build();
        }
    }

    public QueryHits<Section> sectionRegexQuery(String documentId, String word, List<String> phrases, boolean exact,
            boolean disjoint) {
        if (SearchStrategy.MONGO.equals(this.searchStrategy)) {
            return QueryHits.<Section>builder()
                    .baseHits(Sets.newHashSet(this.repository.getSectionsByKeyword(documentId, word)))
                    .noteHits(Sets.newHashSet(/* not supported during mongo search */))
                    .build();
        } else {
            List<SolrSection> solrSections = this.solrSectionRepository.phrasesQuery(documentId, phrases, exact,
                    disjoint);
            List<SolrNote> solrNotes = this.solrNoteRepository.phraseQuery(Optional.of(documentId), Optional.empty(),
                    phrases, exact, disjoint);
            return QueryHits.<Section>builder()
                    .baseHits(Sets.newHashSet(this.repository.getSectionsBySerial(documentId,
                            solrSections.stream().map(SolrSection::getSerial).collect(Collectors.toList()))))
                    .noteHits(Sets.newHashSet(this.repository.getSectionsBySerial(documentId,
                            solrNotes.stream().map(SolrNote::getSection).collect(Collectors.toList()))))
                    .build();
        }
    }

    public QueryHits<Section> sectionParamsQuery(String documentId, List<String> phrases, List<PartOfSpeech> pos,
            int slop) {
        boolean mixed = !phrases.isEmpty() && !pos.isEmpty();
        List<WordTerm> terms = Lists.newArrayList();
        for (String phrase : phrases) {
            terms.add(WordTerm.builder().term(phrase).strategy(MatchingStrategy.CONTAINS).build());
        }
        for (PartOfSpeech partOfSpeech : pos) {
            terms.add(WordTerm.builder().term(partOfSpeech.toString()).strategy(MatchingStrategy.CONTAINS)
                    .build());
        }
        List<SolrSection> solrSections = this.solrSectionRepository.phraseProximityQuery(Optional.of(documentId), terms,
                slop, mixed);
        List<SolrNote> solrNotes = phrases.isEmpty() ? Lists.newArrayList()
                : this.solrNoteRepository.phraseQuery(Optional.of(documentId), Optional.empty(), phrases, false, true);
        return QueryHits.<Section>builder()
                .baseHits(Sets.newHashSet(this.repository.getSectionsBySerial(documentId,
                        solrSections.stream().map(SolrSection::getSerial).collect(Collectors.toList()))))
                .noteHits(Sets.newHashSet(this.repository.getSectionsBySerial(documentId,
                        solrNotes.stream().map(SolrNote::getSection).collect(Collectors.toList()))))
                .build();
    }

    public QueryHits<Section> pageParamsQuery(String documentId, List<String> phrases, List<PartOfSpeech> pos,
            int slop) {
        boolean mixed = !phrases.isEmpty() && !pos.isEmpty();
        List<WordTerm> terms = Lists.newArrayList();
        for (String phrase : phrases) {
            terms.add(WordTerm.builder().term(phrase).strategy(MatchingStrategy.CONTAINS).build());
        }
        for (PartOfSpeech partOfSpeech : pos) {
            terms.add(WordTerm.builder().term(partOfSpeech.toString()).strategy(MatchingStrategy.CONTAINS)
                    .build());
        }
        List<SolrPage> solrSections = this.solrPageRepository.phraseProximityQuery(Optional.of(documentId), terms,
                slop, mixed);
        return QueryHits.<Section>builder()
                .baseHits(Sets.newHashSet(this.repository.getSectionsBySerial(documentId,
                        solrSections.stream().map(SolrPage::getSerial).collect(Collectors.toList()))))
                .noteHits(Sets.newHashSet())
                .build();
    }

    public QueryHits<Section> pageRegexQuery(String documentId, String word, List<String> phrases, boolean exact,
            boolean disjoint) {
        if (SearchStrategy.MONGO.equals(this.searchStrategy)) {
            return QueryHits.<Section>builder()
                    .baseHits(Sets.newHashSet(this.repository.getPagesByKeyword(documentId, word)))
                    .noteHits(Sets.newHashSet())
                    .build();
        } else {
            List<SolrPage> solrPages = this.solrPageRepository.phrasesQuery(documentId, phrases, exact, disjoint);
            return QueryHits.<Section>builder()
                    .baseHits(Sets.newHashSet(this.repository.getPagesBySerial(documentId,
                            solrPages.stream().map(SolrPage::getSerial).collect(Collectors.toList()))))
                    .noteHits(Sets.newHashSet(/* notes have no page information */))
                    .build();
        }
    }

    public Section getSectionBySerial(String documentId, Integer sectionNumber) {
        return this.repository.getSectionBySerial(documentId, sectionNumber);
    }

    public List<Section> getSectionsBySerial(String documentId, List<Long> sectionNumbers) {
        return this.repository.getSectionsBySerial(documentId, sectionNumbers);
    }

    public Section getPageBySerial(String documentId, Integer sectionNumber) {
        return this.repository.getPageBySerial(documentId, sectionNumber);
    }

    public List<Section> getPagesBySerial(String documentId, List<Long> pageNumbers) {
        return this.repository.getPagesBySerial(documentId, pageNumbers);
    }

    public List<Line> getLinesBySectionAndLineNumbers(String documentId, Integer sectionNumber,
            List<Long> lineNumbers) {
        return this.repository.getSectionLinesBySerial(documentId, sectionNumber, lineNumbers);
    }

    public QueryHits<Line> lineRegexQuery(String documentId, Integer sectionNumber, String word, List<String> phrases,
            boolean exact) {
        if (SearchStrategy.MONGO.equals(this.searchStrategy)) {
            return QueryHits.<Line>builder()
                    .baseHits(Sets.newHashSet(this.repository.getLinesByKeyword(documentId, sectionNumber, word)))
                    .noteHits(Sets.newHashSet())
                    .build();
        } else {
            List<SolrLine> solrLines = this.solrLineRepository.phrasesQuery(documentId, sectionNumber, phrases, exact,
                    true);
            List<SolrNote> solrNotes = this.solrNoteRepository.phraseQuery(Optional.of(documentId),
                    Optional.of(sectionNumber), phrases, exact, true);
            return QueryHits.<Line>builder()
                    .baseHits(Sets.newHashSet(this.repository.getSectionLinesBySerial(documentId, sectionNumber,
                            solrLines.stream().map(SolrLine::getSerial).collect(Collectors.toList()))))
                    .noteHits(Sets.newHashSet(this.repository.getSectionLinesBySerial(documentId, sectionNumber,
                            (List<Long>) solrNotes.stream().map(note -> {
                                if (note.getLineRefs() == null) {
                                    return Lists.newArrayList();
                                } else {
                                    return note.getLineRefs();
                                }
                            }).flatMap(List::stream)
                                    .map(o -> (long) o)
                                    .collect(Collectors.toList()))))
                    .build();
        }
    }

    public List<Line> lineIriQuery(String documentId, Integer sectionSerial, List<String> iris) {
        return this.repository.getLinesByIri(documentId, sectionSerial, iris);
    }

    public List<Section> sectionIriQuery(String documentId, List<String> iris) {
        return this.repository.getSectionsByIri(documentId, iris);
    }

    public List<Section> pageIriQuery(String documentId, List<String> iris) {
        return this.repository.getPagesByIri(documentId, iris);
    }

    public List<Document> getDocumentsWithParams(String entryText, List<PartOfSpeech> partsOfSpeech,
            List<String> entryWords, List<String> documentIds, Integer offset, Integer limit) {
        return this.repository.queryWithParams(entryText, partsOfSpeech, entryWords, documentIds, offset, limit);
    }

    public List<PartOfSpeechStatistics> getPOSStatsByDocument(List<String> documentIds) {
        List<PartOfSpeechStatistics> stats = Lists.newArrayList();
        for (String id : documentIds) {
            stats.add(this.solrArticleRepository.getDocumentPOSStats(id));
        }
        return stats;
    }

    public Set<Document> getDocumentByPhraseProximity(List<WordTerm> terms, int slop, boolean mixed) {
        List<SolrDocument> solrDocs = this.solrDocumentRepository.phraseProximityQuery(terms, slop, mixed);
        return this.repository.findAllByIds(solrDocs.stream().map(SolrDocument::getId).collect(Collectors.toList()));
    }

    public Set<Document> getDocumentByPosPhraseProximity(List<PartOfSpeech> pos, int slop) {
        List<SolrDocument> solrDocs = this.solrDocumentRepository.partOfSpeechProximityQuery(pos, slop);
        return this.repository.findAllByIds(solrDocs.stream().map(SolrDocument::getId).collect(Collectors.toList()));
    }

    public Set<Section> getSectionByPhraseProximity(String documentId, List<WordTerm> terms, int slop, boolean mixed) {
        List<SolrSection> solrSections = this.solrSectionRepository.phraseProximityQuery(
                documentId == null ? Optional.empty() : Optional.of(documentId), terms, slop,
                mixed);
        return Sets.newHashSet(this.repository.getSectionsBySerial(documentId,
                solrSections.stream().map(SolrSection::getSerial).collect(Collectors.toList())));
    }

    public Set<Section> getSectionByPosPhraseProximity(String documentId, List<PartOfSpeech> pos, int slop) {
        List<SolrSection> solrSections = this.solrSectionRepository.partOfSpeechProximityQuery(documentId, pos, slop);
        return Sets.newHashSet(this.repository.getSectionsBySerial(documentId,
                solrSections.stream().map(SolrSection::getSerial).collect(Collectors.toList())));
    }

    public Set<Section> getPagesByPhraseProximity(String documentId, List<WordTerm> terms, int slop, boolean mixed) {
        List<SolrPage> solrPages = this.solrPageRepository.phraseProximityQuery(
                documentId == null ? Optional.empty() : Optional.of(documentId), terms, slop, mixed);
        return Sets.newHashSet(this.repository.getSectionsBySerial(documentId,
                solrPages.stream().map(SolrPage::getSerial).collect(Collectors.toList())));
    }

    public Set<Section> getPagesByPosPhraseProximity(String documentId, List<PartOfSpeech> pos, int slop) {
        List<SolrPage> solrPages = this.solrPageRepository.partOfSpeechProximityQuery(documentId, pos, slop);
        return Sets.newHashSet(this.repository.getSectionsBySerial(documentId,
                solrPages.stream().map(SolrPage::getSerial).collect(Collectors.toList())));
    }

    public Map<String, Integer> getMostFrequentShingles(int limit) {
        return this.solrDocumentRepository.mostFrequentShingles(limit);
    }

    public Map<String, Integer> getMostFrequentPosShingles(List<PartOfSpeech> pos, int limit) {
        return this.solrDocumentRepository.mostFrequentFilteredPosShingles(pos, limit);
    }

    public Map<String, Integer> getMostFrequentShinglesOfSections(String id, int limit) {
        return this.solrSectionRepository.mostFrequentShingles(id, limit);
    }

    public Map<String, Integer> getMostFrequentPosShinglesOfSections(String id, List<PartOfSpeech> pos, int limit) {
        return this.solrSectionRepository.mostFrequentFilteredPosShingles(pos, id, limit);
    }

    public Map<String, Integer> getMostFrequentShinglesOfPages(String id, int limit) {
        return this.solrPageRepository.mostFrequentShingles(id, limit);
    }

    public Map<String, Integer> getMostFrequentPosShinglesOfPages(String id, List<PartOfSpeech> pos, int limit) {
        return this.solrPageRepository.mostFrequentFilteredPosShingles(pos, id, limit);
    }

    public Document normalizeDocument(Document doc, List<Article> articles) {
        // normalize document
        // tokenize input text
        Map<String, Set<String>> replacements = Maps.newHashMap();
        for (Article article : articles) {
            for (FormVariant fv : article.getFormVariants()) {
                for (Inflection inflection : fv.getInflections()) {
                    if (inflection.getExamples() != null && !inflection.getExamples().isEmpty()) {
                        if (replacements.containsKey(inflection.getName())) {
                            replacements.get(inflection.getName()).add(article.getEntryWord().replace(' ', '_'));
                        } else {
                            replacements.put(inflection.getName(),
                                    Sets.newHashSet(article.getEntryWord().replace(' ', '_')));
                        }
                    }
                }
            }
        }
        for (Section section : doc.getSections()) {
            section.setNormalized(new String(this.cleanForNormalization(section.getContent())));
        }
        for (Section page : doc.getPages()) {
            page.setNormalized(new String(this.cleanForNormalization(page.getContent())));
        }
        for (String key : replacements.keySet()) {
            // normalize each page and section
            Pattern p = Pattern.compile("([\\s\\p{Z}]+)(" + Pattern.quote(key) + ")([\\s\\p{Z}]+)");
            for (Section section : doc.getSections()) {
                Matcher m = p.matcher(section.getNormalized());
                section.setNormalized(
                        m.replaceAll("$1" + Matcher.quoteReplacement(key) + "["
                                + Matcher.quoteReplacement(String.join("][", replacements.get(key))) + "]" + "$3"));
            }
            for (Section page : doc.getPages()) {
                Matcher m = p.matcher(page.getNormalized());
                page.setNormalized(
                        m.replaceAll("$1" + Matcher.quoteReplacement(key) + "["
                                + Matcher.quoteReplacement(String.join("][", replacements.get(key))) + "]" + "$3"));
            }
        }
        doc.setNormalized(doc.getSections().stream().map(Section::getNormalized).collect(Collectors.joining("\n")));
        this.updateDocument(doc);
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
        String[] sections = preppedForSectionDivision.split("\\R+\\[[^\\[\\]]+\\]\\R+");
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
                .title(dto.getTitle()).sections(sectionCollection).pages(pageCollection)
                .attachmentIds(Lists.newArrayList()).build();
        document.setIri(this.vocabulary.asResource(document));
        Document result = this.repository.insert(document);

        SolrDocumentWrapper solrEntities = this.createSolrEntitiesForDocument(result);
        this.saveSolrEntities(solrEntities);
        return result;
    }

    public void reindexDocument(String id) {
        Document oldDocument = this.repository.findById(id).orElse(null);
        if (oldDocument == null) {
            return;
        }
        SolrDocumentWrapper solrEntities = this.createSolrEntitiesForDocument(oldDocument);
        this.removeSolrEntitiesForDocument(oldDocument);
        this.saveSolrEntities(solrEntities);
    }

    public Document updateDocument(Document document) {
        Document oldDocument = this.repository.findById(document.getId().toString()).orElse(null);
        if (oldDocument != null) {
            oldDocument.setAuthor(document.getAuthor());
            oldDocument.setContent(document.getContent());
            oldDocument.setCorpus(document.getCorpus());
            oldDocument.setTitle(document.getTitle());
            oldDocument.setSections(document.getSections());
            oldDocument.setPages(document.getPages());
            oldDocument.setAttachmentIds(document.getAttachmentIds());
            oldDocument.setNormalized(document.getNormalized());
            this.repository.save(oldDocument);
            SolrDocumentWrapper solrEntities = this.createSolrEntitiesForDocument(oldDocument);
            this.removeSolrEntitiesForDocument(oldDocument);
            this.saveSolrEntities(solrEntities);
            return oldDocument;
        }
        return null;
    }

    public void removeDocument(Document document) {
        this.repository.delete(document);
        this.removeSolrEntitiesForDocument(document);
        this.deleteDocumentAttachments(document.getId().toString());
    }

    public void removeDocuments(List<Document> documents) {
        this.repository.deleteAll(documents);
        documents.forEach(document -> {
            this.removeSolrEntitiesForDocument(document);
            this.deleteDocumentAttachments(document.getId().toString());
        });
    }

    public String uploadAttachmentForDocument(String documentId, AttachmentDTO attachment) {
        return this.repository.uploadAttachmentForDocument(documentId, attachment);
    }

    public List<GridFsResource> getDocumentAttachments(String documentId) {
        return this.repository.getDocumentAttachments(documentId);
    }

    public GridFsResource getDocumentAttachment(String attachmentId) {
        return this.repository.getAttachment(attachmentId);
    }

    public void deleteDocumentAttachments(String documentId) {
        this.repository.deleteDocumentAttachments(documentId);
    }

    private SolrDocumentWrapper createSolrEntitiesForDocument(Document document) {
        SolrDocument solrDocument = SolrDocument.builder()
                .author(document.getAuthor())
                .content(document.getContent())
                .corpus(document.getCorpus().getId().toString())
                .id(document.getId().toString())
                .normalized(document.getNormalized())
                .title(document.getTitle())
                .build();
        List<SolrSection> sections = Lists.newArrayList();
        document.getSections().forEach(section -> {
            SolrSection solrSection = SolrSection.builder()
                    .content(section.getContent())
                    .documentId(document.getId().toString())
                    .id(section.getIri().toString())
                    .normalized(section.getNormalized())
                    .serial(section.getSerial())
                    .build();
            sections.add(solrSection);
        });
        List<SolrPage> pages = Lists.newArrayList();
        document.getPages().forEach(page -> {
            SolrPage solrPage = SolrPage.builder()
                    .content(page.getContent())
                    .documentId(document.getId().toString())
                    .id(page.getIri().toString())
                    .normalized(page.getNormalized())
                    .serial(page.getSerial())
                    .build();
            pages.add(solrPage);
        });
        List<SolrLine> lines = Lists.newArrayList();
        document.getSections().forEach(section -> {
            section.getLines().forEach(line -> {
                SolrLine solrLine = SolrLine.builder()
                        .content(line.getContent())
                        .documentId(document.getId().toString())
                        .id(line.getIri().toString())
                        .sectionId(section.getIri().toString())
                        .sectionSerial(section.getSerial())
                        .serial(line.getSerial())
                        .build();
                lines.add(solrLine);
            });
        });
        List<SolrWord> words = Lists.newArrayList();
        document.getSections().forEach(section -> {
            section.getLines().forEach(line -> {
                String[] cleanWords = this.cleanForNormalization(line.getContent()).split("[\\s\\p{Z}]+");
                for (int i = 0; i < cleanWords.length; ++i) {
                    SolrWord solrWord = SolrWord.builder()
                            .content(cleanWords[i])
                            .documentId(document.getId().toString())
                            .id(line.getIri().toString() + "/" + Integer.toString(i))
                            .lineId(line.getIri().toString())
                            .sectionId(section.getIri().toString())
                            .build();
                    words.add(solrWord);
                }
            });
        });
        return SolrDocumentWrapper.builder()
                .document(solrDocument)
                .lines(lines)
                .pages(pages)
                .sections(sections)
                .words(words)
                .build();
    }

    private void saveSolrEntities(SolrDocumentWrapper entities) {
        this.solrDocumentRepository.save(entities.getDocument());
        this.solrPageRepository.saveAll(entities.getPages());
        this.solrSectionRepository.saveAll(entities.getSections());
        this.solrLineRepository.saveAll(entities.getLines());
        this.solrWordRepository.saveAll(entities.getWords());
    }

    private void removeSolrEntitiesForDocument(Document document) {
        String id = document.getId().toString();
        this.solrDocumentRepository.deleteById(id);
        this.solrPageRepository.deleteAll(this.solrPageRepository.findByDocumentId(id));
        this.solrSectionRepository.deleteAll(this.solrSectionRepository.findByDocumentId(id));
        this.solrLineRepository.deleteAll(this.solrLineRepository.findByDocumentId(id));
        this.solrWordRepository.deleteAll(this.solrWordRepository.findByDocumentId(id));
    }

    public String cleanForNormalization(String input) {
        // clean some kind of references
        String cleaned = input.replaceAll("\\s*-?\\[.+\\]\\s*", "");
        // clean hypehenation
        cleaned = cleaned.replaceAll("(\\S+)-\\n(\\S+)", "$1$2\n");
        // clean number appended to words
        cleaned = cleaned.replaceAll("\\h+([^\\d\\s]+)\\d+", " $1 ");
        // clean punctuation
        cleaned = cleaned.replaceAll("[\\ufeff.,;?!:)(]", " ");
        // collapse whitespace
        cleaned = cleaned.replaceAll("\\h+", " ");
        // leading and trailing whitespace removal
        cleaned = cleaned.replaceAll("(?m)^\\h+", "").replaceAll("(?m)\\h+$", "");
        // clean trailing hyphens
        cleaned = cleaned.replaceAll("(?m)-$", "");
        return cleaned;
    }
}
