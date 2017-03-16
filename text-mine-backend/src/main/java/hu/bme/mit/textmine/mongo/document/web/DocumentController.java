package hu.bme.mit.textmine.mongo.document.web;

import java.util.Collection;
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

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;

@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        path = "/documents")
public class DocumentController {

    @Autowired
    private DocumentService service;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Document>> getAll() {
        List<Document> documents = this.service.getAllDocuments();
        if (documents == null || documents.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
    
    @RequestMapping(method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
    public ResponseEntity<Document> postFile(MultipartHttpServletRequest request) {
        String author = request.getParameter("author");
        String title = request.getParameter("title");
        String corpusId = request.getParameter("corpusId");
        // TODO corpus
        MultipartFile file = request.getFile("file");
        if(Stream.of(author, title, corpusId, file).allMatch(Objects::nonNull)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
