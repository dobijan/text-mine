package hu.bme.mit.textmine.mongo.document.web;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
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

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.mongo.corpus.service.CorpusService;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.DocumentFileDTO;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/documents")
public class DocumentController {

    @Autowired
    private DocumentService service;

    @Autowired
    private CorpusService corpusService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Document>> getAll() {
        List<Document> documents = this.service.getAllDocuments();
        if (documents == null || documents.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

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
            return new ResponseEntity<>(result, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{id}/normalize", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> normalize(@PathVariable("id") String id) {
        Document doc = this.service.normalizeDocument(id);
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
