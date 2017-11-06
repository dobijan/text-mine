package hu.bme.mit.textmine.mongo.document.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.mongo.corpus.service.CorpusService;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.dictionary.service.ArticleService;
import hu.bme.mit.textmine.mongo.document.model.AttachmentDTO;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.DocumentFileDTO;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.QueryHits;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import hu.bme.mit.textmine.solr.model.PartOfSpeechStatistics;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/documents")
public class DocumentController {

    @Autowired
    @Lazy
    private DocumentService service;

    @Autowired
    private CorpusService corpusService;

    @Autowired
    private ArticleService articleService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Document>> getFiltered(
            @RequestParam(required = false) String entryText,
            @RequestParam(required = false, name = "partOfSpeech") List<String> partsOfSpeech,
            @RequestParam(required = false, name = "entryWord") List<String> entryWords,
            @RequestParam(required = false, name = "documentId") List<String> documentIds,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        List<PartOfSpeech> poss = PartOfSpeech.of(partsOfSpeech);
        List<Document> documents = this.service.getDocumentsWithParams(entryText, poss, entryWords, documentIds, offset,
                limit);
        if (documents == null || documents.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<Document> getOne(@PathVariable("id") String id) {
        Document document = this.service.getDocument(id);
        if (document == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/by-corpus/{id}")
    public ResponseEntity<List<Document>> getByCorpus(@PathVariable("id") String id) {
        List<Document> documents = this.service.getDocumentsByCorpus(id);
        if (documents == null || documents.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/sections/{id}")
    public ResponseEntity<QueryHits<Section>> getFilteredSections(
            @PathVariable("id") String id,
            @RequestParam(required = false, value = "phrase") String[] phrases,
            @RequestParam(required = false, value = "pos") String[] partsOfSpeech,
            @RequestParam(required = false, value = "slop", defaultValue = "0") Integer slop,
            @RequestParam(required = false, value = "disjoint", defaultValue = "true") Boolean disjoint) {
        List<String> phrasesList = phrases == null ? Lists.newArrayList() : Arrays.asList(phrases);
        List<PartOfSpeech> poss = partsOfSpeech == null ? Lists.newArrayList()
                : PartOfSpeech.of(Arrays.asList(partsOfSpeech));
        QueryHits<Section> hits = disjoint ? this.service.sectionParamsQuery(id, phrasesList, poss, slop)
                : this.service.sectionRegexQuery(id, null, phrasesList, false, disjoint);
        if (hits == null || (hits.getBaseHits().isEmpty() && hits.getNoteHits().isEmpty())) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(hits, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/pages/{id}")
    public ResponseEntity<QueryHits<Section>> getFilteredPages(
            @PathVariable("id") String id,
            @RequestParam(required = false, value = "phrase") String[] phrases,
            @RequestParam(required = false, value = "pos") String[] partsOfSpeech,
            @RequestParam(required = false, value = "slop", defaultValue = "0") Integer slop,
            @RequestParam(required = false, value = "disjoint", defaultValue = "true") Boolean disjoint) {
        List<String> phrasesList = phrases == null ? Lists.newArrayList() : Arrays.asList(phrases);
        List<PartOfSpeech> poss = partsOfSpeech == null ? Lists.newArrayList()
                : PartOfSpeech.of(Arrays.asList(partsOfSpeech));
        QueryHits<Section> hits = disjoint ? this.service.pageParamsQuery(id, phrasesList, poss, slop)
                : this.service.pageRegexQuery(id, null, phrasesList, false, disjoint);
        if (hits == null || (hits.getBaseHits().isEmpty() && hits.getNoteHits().isEmpty())) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(hits, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/sections/{id}/lines/{serial}")
    public ResponseEntity<QueryHits<Line>> getFilteredLines(
            @PathVariable("id") String id,
            @PathVariable("serial") Integer serial,
            @RequestParam(required = false, value = "phrase") String[] phrases) {
        List<String> phrasesList = Arrays.asList(phrases);
        QueryHits<Line> hits = this.service.lineRegexQuery(id, serial, "(" + String.join("|", phrases) + ")",
                phrasesList, false);
        if (hits == null || (hits.getBaseHits().isEmpty() && hits.getNoteHits().isEmpty())) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(hits, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/pos/stats/{id}")
    public ResponseEntity<List<PartOfSpeechStatistics>> getPosStats(@PathVariable("id") String id) {
        List<PartOfSpeechStatistics> stats = this.service.getPOSStatsByDocument(Lists.newArrayList(id));
        if (stats.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/pos/shingle/stats")
    public ResponseEntity<List<Entry<String, Integer>>> getPosShingleStats(
            @RequestParam(value = "pos") String[] partsOfSpeech,
            @RequestParam(required = false, value = "limit", defaultValue = "1000") Integer limit) {
        List<PartOfSpeech> poss = partsOfSpeech == null ? Lists.newArrayList()
                : PartOfSpeech.of(Arrays.asList(partsOfSpeech));
        Map<String, Integer> entries = this.service.getMostFrequentPosShingles(poss, limit);
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(entries.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/shingle/stats")
    public ResponseEntity<List<Entry<String, Integer>>> getShingleStats(
            @RequestParam(required = false, value = "limit", defaultValue = "1000") Integer limit) {
        Map<String, Integer> entries = this.service.getMostFrequentShingles(limit);
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(entries.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/sections/{id}/shingle/stats")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getSectionShingleStats(
            @PathVariable("id") String id,
            @RequestParam(required = false, value = "limit", defaultValue = "1000") Integer limit) {
        Map<String, Integer> entries = this.service.getMostFrequentShinglesOfSections(id, limit);
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(entries.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/pages/{id}/shingle/stats")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getPageShingleStats(
            @PathVariable("id") String id,
            @RequestParam(required = false, value = "limit", defaultValue = "1000") Integer limit) {
        Map<String, Integer> entries = this.service.getMostFrequentShinglesOfPages(id, limit);
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(entries.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/sections/{id}/pos/shingle/stats")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getSectionPosShingleStats(
            @PathVariable("id") String id,
            @RequestParam(value = "pos") String[] partsOfSpeech,
            @RequestParam(required = false, value = "limit", defaultValue = "1000") Integer limit) {
        List<PartOfSpeech> poss = partsOfSpeech == null ? Lists.newArrayList()
                : PartOfSpeech.of(Arrays.asList(partsOfSpeech));
        Map<String, Integer> entries = this.service.getMostFrequentPosShinglesOfSections(id, poss, limit);
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(entries.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/pages/{id}/pos/shingle/stats")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getPagePosShingleStats(
            @PathVariable("id") String id,
            @RequestParam(value = "pos") String[] partsOfSpeech,
            @RequestParam(required = false, value = "limit", defaultValue = "1000") Integer limit) {
        List<PartOfSpeech> poss = partsOfSpeech == null ? Lists.newArrayList()
                : PartOfSpeech.of(Arrays.asList(partsOfSpeech));
        Map<String, Integer> entries = this.service.getMostFrequentPosShinglesOfPages(id, poss, limit);
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(entries.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/reindex/{id}")
    public ResponseEntity<Document> reindex(@PathVariable("id") String id) {
        Document document = this.service.getDocument(id);
        this.service.reindexDocument(id);
        if (document == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Document> postFile(MultipartHttpServletRequest request) throws IOException {
        String author = request.getParameter("author");
        String title = request.getParameter("title");
        String corpusId = request.getParameter("corpusId");
        Corpus corpus = this.corpusService.getCorpus(corpusId);
        if (corpus == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        MultipartFile file = request.getFile("file");
        String ct = file.getContentType();
        if (Stream.of(author, title, corpusId, file).allMatch(Objects::nonNull)
                && MediaType.TEXT_PLAIN.toString().equals(ct)) {
            Document result = this.service.createDocument(
                    DocumentFileDTO.builder().author(author).corpusId(corpusId).file(file).title(title).build());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{id}/attach", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> uploadAttachment(@PathVariable("id") String id, MultipartHttpServletRequest request) {
        if (!this.service.exists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        MultipartFile file = request.getFile("file");
        Map<String, String> metadata = Maps.newHashMap();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            metadata.put(param, request.getParameter(param));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.uploadAttachmentForDocument(id,
                AttachmentDTO.builder().content(file).metadata(metadata).build()));
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = "/attachment/{attachmentId}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getFile(@PathVariable("attachmentId") String attachmentId) throws IOException {

        GridFsResource file = this.service.getDocumentAttachment(attachmentId);
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        byte[] content = IOUtils.toByteArray(file.getInputStream());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFilename());
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, file.getContentType());

        return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).body(content);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}/attachments")
    public ResponseEntity<String> deleteAttachments(@PathVariable("id") String id) {
        this.service.deleteDocumentAttachments(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{id}/normalize", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> normalize(@PathVariable("id") String id) {
        Document doc = this.service.getDocument(id);
        doc = this.service.normalizeDocument(doc, this.articleService.getArticlesByDocument(id));
        if (doc == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(doc.getNormalized(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity<Document> put(@RequestBody Document document, @PathVariable("id") String id) {
        Document oldDocument = this.service.getDocument(id);
        if (oldDocument == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Document newDocument = this.service.updateDocument(document);
        return new ResponseEntity<>(newDocument, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") String id) {
        Document document = this.service.getDocument(id);
        if (document == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.service.removeDocument(document);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
