package hu.bme.mit.textmine.mongo.dictionary.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.google.common.collect.Maps;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.ArticleDTO;
import hu.bme.mit.textmine.mongo.dictionary.model.ArticleFileDTO;
import hu.bme.mit.textmine.mongo.dictionary.model.DocumentArticles;
import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeechCsvBean;
import hu.bme.mit.textmine.mongo.dictionary.service.ArticleService;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private DocumentService documentService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ArticleDTO>> getFiltered(
            @RequestParam(required = false) String entryWord,
            @RequestParam(required = false) String formVariant,
            @RequestParam(required = false) String inflection,
            @RequestParam(required = false) String partOfSpeech,
            @RequestParam(required = false, name = "documentId") List<String> documentIds,
            @RequestParam(required = false) String corpusId,
            @RequestParam(required = false, defaultValue = "EXACT_MATCH") MatchingStrategy matchingStrategy,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "100") Integer limit) {
        PartOfSpeech pos = PartOfSpeech.of(partOfSpeech);
        List<Article> articles = this.articleService.queryWithParams(entryWord, formVariant, inflection, pos,
                documentIds, corpusId, matchingStrategy, offset, limit);
        if (articles == null || articles.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ArticleDTO.from(articles), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<ArticleDTO> getOne(@PathVariable("id") String id) {
        Article article = this.articleService.getArticle(id);
        if (article == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ArticleDTO.from(article), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/group/by-document")
    public ResponseEntity<Map<Object, List<ArticleDTO>>> groupByDocument(
            @RequestParam(required = false) String entryWord,
            @RequestParam(required = false) String formVariant,
            @RequestParam(required = false) String inflection,
            @RequestParam(required = false) List<String> partOfSpeech,
            @RequestParam(required = false) String corpusId,
            @RequestParam(required = false, defaultValue = "EXACT_MATCH") MatchingStrategy matchingStrategy,
            @RequestParam(required = false) Integer posCount) {
        List<PartOfSpeech> pos = partOfSpeech.stream().map(PartOfSpeech::of).collect(Collectors.toList());
        List<DocumentArticles> groups = this.articleService.findByDocumentWithParams(entryWord, formVariant, inflection,
                pos, matchingStrategy, posCount);
        if (groups == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (groups.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        Map<Object, List<ArticleDTO>> result = Maps.newHashMap();
        for (DocumentArticles group : groups) {
            result.put(group.getArticles().get(0).getId().toString(), ArticleDTO.from(group.getArticles()));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/by-document/{documentId}")
    public ResponseEntity<List<ArticleDTO>> getByDocument(
            @PathVariable("documentId") String documentId,
            @RequestParam(required = false) Integer section,
            @RequestParam(required = false) Integer page) {
        if (section != null && page != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        List<Article> articles = null;
        if (page != null) {
            articles = this.articleService.findInDocumentPage(documentId, page);
        } else if (section != null) {
            articles = this.articleService.findInDocumentSection(documentId, page);
        } else {
            articles = this.articleService.getArticlesByDocument(documentId);
        }
        if (articles == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (articles.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(ArticleDTO.from(articles), HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ArticleDTO> postArticle(MultipartHttpServletRequest request) throws IOException {
        ArticleFileDTO dto = new ArticleFileDTO();
        ResponseEntity<ArticleDTO> check = this.checkFileBasedPost(request, dto);
        if (check != null) {
            return check;
        }
        return new ResponseEntity<>(ArticleDTO.from(this.articleService.createArticle(dto)), HttpStatus.CREATED);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.POST, path = "/multiple")
    public ResponseEntity<List<ArticleDTO>> postArticles(MultipartHttpServletRequest request) throws IOException {
        ArticleFileDTO dto = new ArticleFileDTO();
        ResponseEntity<List<ArticleDTO>> check = this.checkFileBasedPost(request, dto);
        if (check != null) {
            return check;
        }
        return new ResponseEntity<>(ArticleDTO.from(this.articleService.createMultipleArticles(dto)),
                HttpStatus.CREATED);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.POST, path = "/pos")
    public ResponseEntity<List<PartOfSpeechCsvBean>> postPartsOfSpeech(MultipartHttpServletRequest request)
            throws IOException {
        ArticleFileDTO dto = new ArticleFileDTO();
        ResponseEntity<List<PartOfSpeechCsvBean>> check = this.checkFileBasedPost(request, dto);
        if (check != null) {
            return check;
        }
        return new ResponseEntity<>(this.articleService.attachPOSInfo(dto), HttpStatus.ACCEPTED);
    }

    @SuppressWarnings("rawtypes")
    private ResponseEntity checkFileBasedPost(MultipartHttpServletRequest request, ArticleFileDTO dto) {
        String documentId = request.getParameter("documentId");
        MultipartFile file = request.getFile("file");
        if (documentId == null || file == null) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.documentService.exists(documentId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // if (!MediaType.TEXT_PLAIN.toString().equals(file.getContentType())
        // && !MediaType.APPLICATION_XML.toString().equals(file.getContentType())
        // && !MediaType.TEXT_XML.toString().equals(file.getContentType())) {
        // return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        // }
        dto.setFile(file);
        dto.setDocumentId(documentId);
        return null;
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity<ArticleDTO> putArticle(@RequestBody Article article, @PathVariable("id") String id) {
        Article oldArticle = this.articleService.getArticle(id);
        if (oldArticle == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Article newArticle = this.articleService.updateArticle(oldArticle);
        return new ResponseEntity<>(ArticleDTO.from(newArticle), HttpStatus.ACCEPTED);
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
