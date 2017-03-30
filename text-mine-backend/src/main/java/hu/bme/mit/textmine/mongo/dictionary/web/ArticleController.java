package hu.bme.mit.textmine.mongo.dictionary.web;

import java.io.IOException;
import java.util.List;

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

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.ArticleFileDTO;
import hu.bme.mit.textmine.mongo.dictionary.service.ArticleService;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/articles")
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private DocumentService documentService;
    
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Article>> getAll() {
        List<Article> articles = this.articleService.getAll();
        if (articles.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<Article> getOne(@PathVariable("id") String id) {
        Article article = this.articleService.getArticle(id);
        if (article == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(article, HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.GET, path = "/by-document/{documentId}")
    public ResponseEntity<List<Article>> getByDocument(@PathVariable("documentId") String documentId) {
        List<Article> articles = this.articleService.getArticlesByDocument(documentId);
        if (articles == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (articles.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Article> postArticle(MultipartHttpServletRequest request) throws IOException {
        String documentId = request.getParameter("documentId");
        MultipartFile file = request.getFile("file");
        if (documentId == null || file == null) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        Document document = this.documentService.getDocument(documentId);
        if (document == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!MediaType.TEXT_PLAIN.toString().equals(file.getContentType())) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        ArticleFileDTO dto = ArticleFileDTO.builder().documentId(documentId).file(file).build();
        return new ResponseEntity<>(this.articleService.createArticle(dto), HttpStatus.CREATED);
    }
    
    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity<Article> putArticle(@RequestBody Article article, @PathVariable("id") String id) {
        Article oldArticle = this.articleService.getArticle(id);
        if (oldArticle == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Article newArticle = this.articleService.updateArticle(oldArticle);
        return new ResponseEntity<>(newArticle, HttpStatus.ACCEPTED);
    }
    
    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<String> deleteArticle(@PathVariable("id") String id) {
        Article article = this.articleService.getArticle(id);
        if (article == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.articleService.removeArticle(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
