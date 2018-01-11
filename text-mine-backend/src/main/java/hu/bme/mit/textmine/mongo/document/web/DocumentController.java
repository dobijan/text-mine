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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "/documents")
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

    private static final class LineHits extends QueryHits<Line> {
    }

    private static final class SectionHits extends QueryHits<Section> {
    }

    private static abstract class PosShingleEntry implements Entry<String, Integer> {
    }

    @ApiOperation(
            value = "Get filtered documents",
            httpMethod = "GET",
            response = Document.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Document>> getFiltered(
            @ApiParam(value = "Entry text", required = false, name = "entryText") @RequestParam(
                    name = "entryText",
                    required = false) String entryText,
            @ApiParam(value = "Parts of speech", required = false, allowMultiple = true, name = "pos") @RequestParam(
                    required = false,
                    name = "pos") List<String> partsOfSpeech,
            @ApiParam(value = "Entry words", required = false, allowMultiple = true, name = "entryWord") @RequestParam(
                    required = false,
                    name = "entryWord") List<String> entryWords,
            @ApiParam(
                    value = "Document ids",
                    required = false,
                    allowMultiple = true,
                    name = "documentId") @RequestParam(required = false, name = "documentId") List<String> documentIds,
            @ApiParam(value = "Offset", required = false, name = "offset") @RequestParam(
                    name = "offset",
                    required = false,
                    defaultValue = "0") Integer offset,
            @ApiParam(value = "Limit", required = false, name = "limit") @RequestParam(
                    name = "limit",
                    required = false,
                    defaultValue = "100") Integer limit) {
        List<PartOfSpeech> poss = PartOfSpeech.of(partsOfSpeech);
        List<Document> documents = this.service.getDocumentsWithParams(entryText,
                poss == null || poss.size() == 0 ? null : poss, entryWords, documentIds, offset, limit);
        if (documents == null || documents.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get document by Id",
            httpMethod = "GET",
            response = Document.class)
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<Document> getOne(
            @ApiParam(value = "Document Id", required = true) @PathVariable("id") String id) {
        Document document = this.service.getDocument(id);
        if (document == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get document by corpus id",
            httpMethod = "GET",
            response = Document.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/by-corpus/{id}")
    public ResponseEntity<List<Document>> getByCorpus(
            @ApiParam(value = "Corpus Id", required = true) @PathVariable("id") String id) {
        List<Document> documents = this.service.getDocumentsByCorpus(id);
        if (documents == null || documents.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get filtered sections",
            httpMethod = "GET",
            response = SectionHits.class)
    @RequestMapping(method = RequestMethod.GET, path = "/sections/{id}")
    public ResponseEntity<QueryHits<Section>> getFilteredSections(
            @ApiParam(value = "Document Id", required = true) @PathVariable("id") String id,
            @ApiParam(value = "Phrases", required = false, allowMultiple = true, name = "phrase") @RequestParam(
                    required = false,
                    name = "phrase") String[] phrases,
            @ApiParam(value = "Parts of speech", required = false, allowMultiple = true, name = "pos") @RequestParam(
                    required = false,
                    name = "pos") String[] partsOfSpeech,
            @ApiParam(value = "Sloppy distance", required = false, name = "slop") @RequestParam(
                    name = "slop",
                    required = false,
                    value = "slop",
                    defaultValue = "0") Integer slop,
            @ApiParam(value = "Collections are ALL or ANY?", required = false, name = "disjoint") @RequestParam(
                    name = "disjoint",
                    required = false,
                    value = "disjoint",
                    defaultValue = "true") Boolean disjoint) {
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

    @ApiOperation(
            value = "Get filtered sections",
            httpMethod = "GET",
            response = SectionHits.class)
    @RequestMapping(method = RequestMethod.GET, path = "/pages/{id}")
    public ResponseEntity<QueryHits<Section>> getFilteredPages(
            @ApiParam(value = "Document Id", required = true) @PathVariable("id") String id,
            @ApiParam(value = "Phrases", required = false, allowMultiple = true, name = "phrase") @RequestParam(
                    required = false,
                    name = "phrase") String[] phrases,
            @ApiParam(value = "Parts of speech", required = false, allowMultiple = true, name = "pos") @RequestParam(
                    required = false,
                    name = "pos") String[] partsOfSpeech,
            @ApiParam(value = "Sloppy distance", required = false, name = "slop") @RequestParam(
                    name = "slop",
                    required = false,
                    value = "slop",
                    defaultValue = "0") Integer slop,
            @ApiParam(value = "Collections are ALL or ANY?", required = false, name = "disjoint") @RequestParam(
                    name = "disjoint",
                    required = false,
                    value = "disjoint",
                    defaultValue = "true") Boolean disjoint) {
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

    @ApiOperation(
            value = "Get filtered lines of section",
            httpMethod = "GET",
            response = LineHits.class)
    @RequestMapping(method = RequestMethod.GET, path = "/sections/{id}/lines/{serial}")
    public ResponseEntity<QueryHits<Line>> getFilteredLines(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id,
            @ApiParam(value = "Section number", required = true) @PathVariable("serial") Integer serial,
            @ApiParam(value = "Phrases", required = false, allowMultiple = true, name = "phrase") @RequestParam(
                    required = false,
                    name = "phrase") String[] phrases) {
        List<String> phrasesList = Arrays.asList(phrases);
        QueryHits<Line> hits = this.service.lineRegexQuery(id, serial, "(" + String.join("|", phrases) + ")",
                phrasesList, false);
        if (hits == null || (hits.getBaseHits().isEmpty() && hits.getNoteHits().isEmpty())) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(hits, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get PartOfSpeech statistics",
            httpMethod = "GET",
            response = PartOfSpeechStatistics.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/pos/stats/{id}")
    public ResponseEntity<List<PartOfSpeechStatistics>> getPosStats(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id) {
        List<PartOfSpeechStatistics> stats = this.service.getPOSStatsByDocument(Lists.newArrayList(id));
        if (stats.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get PartOfSpeech shingle statistics",
            httpMethod = "GET",
            response = PosShingleEntry.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/pos/shingle/stats")
    public ResponseEntity<List<Entry<String, Integer>>> getPosShingleStats(
            @ApiParam(value = "Parts of speech", required = false, allowMultiple = true, name = "pos") @RequestParam(
                    name = "pos") String[] partsOfSpeech,
            @ApiParam(value = "Limit", required = false, name = "limit") @RequestParam(
                    required = false,
                    name = "limit",
                    defaultValue = "1000") Integer limit) {
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

    @ApiOperation(
            value = "Get shingle statistics",
            httpMethod = "GET",
            response = PosShingleEntry.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/shingle/stats")
    public ResponseEntity<List<Entry<String, Integer>>> getShingleStats(
            @ApiParam(value = "Limit", required = false, name = "limit") @RequestParam(
                    required = false,
                    name = "limit",
                    defaultValue = "1000") Integer limit) {
        Map<String, Integer> entries = this.service.getMostFrequentShingles(limit);
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(entries.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get section shingle statistics",
            httpMethod = "GET",
            response = PosShingleEntry.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/sections/{id}/shingle/stats")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getSectionShingleStats(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id,
            @ApiParam(value = "Limit", required = false, name = "limit") @RequestParam(
                    required = false,
                    name = "limit",
                    defaultValue = "1000") Integer limit) {
        Map<String, Integer> entries = this.service.getMostFrequentShinglesOfSections(id, limit);
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(entries.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get page shingle statistics",
            httpMethod = "GET",
            response = PosShingleEntry.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/pages/{id}/shingle/stats")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getPageShingleStats(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id,
            @ApiParam(value = "Limit", required = false, name = "limit") @RequestParam(
                    required = false,
                    name = "limit",
                    defaultValue = "1000") Integer limit) {
        Map<String, Integer> entries = this.service.getMostFrequentShinglesOfPages(id, limit);
        if (entries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(entries.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get section PartOfSpeech shingle statistics",
            httpMethod = "GET",
            response = PosShingleEntry.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/sections/{id}/pos/shingle/stats")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getSectionPosShingleStats(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id,
            @ApiParam(value = "Parts of speech", required = false, allowMultiple = true, name = "pos") @RequestParam(
                    name = "pos") String[] partsOfSpeech,
            @ApiParam(value = "Limit", required = false, name = "limit") @RequestParam(
                    required = false,
                    name = "limit",
                    defaultValue = "1000") Integer limit) {
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

    @ApiOperation(
            value = "Get page PartOfSpeech shingle statistics",
            httpMethod = "GET",
            response = PosShingleEntry.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/pages/{id}/pos/shingle/stats")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getPagePosShingleStats(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id,
            @ApiParam(value = "Parts of speech", required = false, allowMultiple = true, name = "pos") @RequestParam(
                    name = "pos") String[] partsOfSpeech,
            @ApiParam(value = "Limit", required = false, name = "limit") @RequestParam(
                    required = false,
                    name = "limit",
                    defaultValue = "1000") Integer limit) {
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

    @ApiOperation(
            value = "Get page PartOfSpeech shingle statistics",
            httpMethod = "POST",
            response = Document.class)
    @RequestMapping(method = RequestMethod.POST, path = "/reindex/{id}")
    public ResponseEntity<Document> reindex(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id) {
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

    @ApiOperation(
            value = "Get attachment by id",
            httpMethod = "GET",
            response = byte[].class,
            produces = "application/octet-stream")
    @RequestMapping(
            method = RequestMethod.GET,
            path = "/attachment/{attachmentId}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getFile(
            @ApiParam(value = "Attachment id", required = true) @PathVariable("attachmentId") String attachmentId)
            throws IOException {

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

    @ApiOperation(
            value = "Delete document by id",
            httpMethod = "DELETE",
            response = String.class)
    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}/attachments")
    public ResponseEntity<String> deleteAttachments(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id) {
        this.service.deleteDocumentAttachments(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ApiOperation(
            value = "Normalize document by id",
            httpMethod = "POST",
            response = String.class)
    @RequestMapping(method = RequestMethod.POST, path = "/{id}/normalize", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> normalize(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id) {
        Document doc = this.service.getDocument(id);
        doc = this.service.normalizeDocument(doc, this.articleService.getArticlesByDocument(id));
        if (doc == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(doc.getNormalized(), HttpStatus.OK);
    }

    @ApiOperation(
            value = "Update document by id",
            httpMethod = "PUT",
            response = Document.class)
    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity<Document> put(
            @ApiParam(value = "Document", required = true) @RequestBody Document document,
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id) {
        Document oldDocument = this.service.getDocument(id);
        if (oldDocument == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Document newDocument = this.service.updateDocument(document);
        return new ResponseEntity<>(newDocument, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete document by id",
            httpMethod = "DELETE",
            response = String.class)
    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<String> delete(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id) {
        Document document = this.service.getDocument(id);
        if (document == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.service.removeDocument(document);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
