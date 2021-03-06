package hu.bme.mit.textmine.mongo.dictionary.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.ArticleDTO;
import hu.bme.mit.textmine.mongo.dictionary.model.ArticleFileDTO;
import hu.bme.mit.textmine.mongo.dictionary.model.DocumentArticles;
import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeechCsvBean;
import hu.bme.mit.textmine.mongo.dictionary.service.ArticleService;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

@Api(value = "/articles")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    @Lazy
    private DocumentService documentService;

    @Getter
    @Setter
    private static class ListResult<T> {

        public long size = 0;
        public long rangeFrom = 0;
        public long rangeSize = 0;
        public List<T> entries = Lists.newArrayList();
    }

    @Getter
    @Setter
    private static class ArticleDTOList extends ListResult<ArticleDTO> {

        public List<ArticleDTO> entries;

    }

    @ApiOperation(
            value = "Get filtered articles",
            httpMethod = "GET",
            response = ArticleDTO.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ArticleDTO>> getFiltered(
            @ApiParam(value = "Entry word", required = false, name = "entryWord") @RequestParam(
                    name = "entryWord",
                    required = false) String entryWord,
            @ApiParam(value = "Form variant", required = false, name = "formVariant") @RequestParam(
                    name = "formVariant",
                    required = false) String formVariant,
            @ApiParam(value = "Inflection", required = false, name = "inflection") @RequestParam(
                    name = "inflection",
                    required = false) String inflection,
            @ApiParam(value = "Parts of speech", required = false, allowMultiple = true, name = "pos") @RequestParam(
                    name = "pos",
                    required = false) List<String> partOfSpeech,
            @ApiParam(
                    value = "Document ids",
                    required = false,
                    allowMultiple = true,
                    name = "documentId") @RequestParam(required = false, name = "documentId") List<String> documentIds,
            @ApiParam(value = "Corpus id", required = false, name = "corpusId") @RequestParam(
                    name = "corpusId",
                    required = false) String corpusId,
            @ApiParam(value = "Matching strategy", required = false, name = "matchingStrategy") @RequestParam(
                    name = "matchingStrategy",
                    required = false,
                    defaultValue = "EXACT_MATCH") MatchingStrategy matchingStrategy,
            @ApiParam(value = "Offset", required = false, name = "offset") @RequestParam(
                    name = "offset",
                    required = false,
                    defaultValue = "0") Integer offset,
            @ApiParam(value = "Limit", required = false, name = "limit") @RequestParam(
                    name = "limit",
                    required = false,
                    defaultValue = "100") Integer limit) {
        List<PartOfSpeech> pos = partOfSpeech != null ? PartOfSpeech.of(partOfSpeech) : null;
        List<Article> articles = this.articleService.queryWithParams(entryWord, formVariant, inflection, pos,
                documentIds, corpusId, matchingStrategy, offset, limit);
        if (articles == null || articles.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ArticleDTO.from(articles), HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get article by Id",
            httpMethod = "GET",
            response = ArticleDTO.class)
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<ArticleDTO> getOne(
            @ApiParam(value = "Article Id", required = true) @PathVariable("id") String id) {
        Article article = this.articleService.getArticle(id);
        if (article == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ArticleDTO.from(article), HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get articles grouped by document id",
            httpMethod = "GET",
            response = ArticleDTOList.class,
            responseContainer = "Map")
    @RequestMapping(method = RequestMethod.GET, path = "/group/by-document")
    public ResponseEntity<Map<String, List<ArticleDTO>>> groupByDocument(
            @ApiParam(value = "Entry word", required = false, name = "entryWord") @RequestParam(
                    name = "entryWord",
                    required = false) String entryWord,
            @ApiParam(value = "Form variant", required = false, name = "formVariant") @RequestParam(
                    name = "formVariant",
                    required = false) String formVariant,
            @ApiParam(value = "Inflection", required = false, name = "inflection") @RequestParam(
                    name = "inflection",
                    required = false) String inflection,
            @ApiParam(value = "Parts of speech", required = false, allowMultiple = true, name = "pos") @RequestParam(
                    name = "pos",
                    required = false) List<String> partOfSpeech,
            @ApiParam(value = "Corpus Id", required = false, name = "corpusId") @RequestParam(
                    name = "corpusId",
                    required = false) String corpusId,
            @ApiParam(value = "Matching strategy", required = false, name = "matchingStrategy") @RequestParam(
                    name = "matchingStrategy",
                    required = false,
                    defaultValue = "EXACT_MATCH") MatchingStrategy matchingStrategy,
            @ApiParam(value = "Part of speech count", required = false, name = "posCount") @RequestParam(
                    name = "posCount",
                    required = false) Integer posCount) {
        List<PartOfSpeech> pos = partOfSpeech != null ? PartOfSpeech.of(partOfSpeech) : null;
        List<DocumentArticles> groups = this.articleService.findByDocumentWithParams(entryWord, formVariant, inflection,
                pos, matchingStrategy, posCount);
        if (groups == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (groups.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        Map<String, List<ArticleDTO>> result = Maps.newHashMap();
        for (DocumentArticles group : groups) {
            result.put(group.getArticles().get(0).getDocumentId(), ArticleDTO.from(group.getArticles()));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get articles of a document",
            httpMethod = "GET",
            response = ArticleDTO.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/by-document/{documentId}")
    public ResponseEntity<List<ArticleDTO>> getByDocument(
            @ApiParam(value = "Document Id", required = true) @PathVariable("documentId") String documentId,
            @ApiParam(value = "Section number", required = false, name = "section") @RequestParam(
                    name = "section",
                    required = false) Integer section,
            @ApiParam(value = "Page number", required = false, name = "page") @RequestParam(
                    name = "page",
                    required = false) Integer page) {
        if (section != null && page != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        List<Article> articles = null;
        if (page != null) {
            articles = this.articleService.findInDocumentPage(documentId, page);
        } else if (section != null) {
            articles = this.articleService.findInDocumentSection(documentId, section);
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

    @ApiOperation(
            value = "Update article",
            httpMethod = "PUT",
            response = ArticleDTO.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity<ArticleDTO> putArticle(
            @ApiParam(value = "Article", required = true) @RequestBody Article article,
            @ApiParam(value = "Article id", required = true) @PathVariable("id") String id) {
        Article oldArticle = this.articleService.getArticle(id);
        if (oldArticle == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Article newArticle = this.articleService.updateArticle(oldArticle);
        return new ResponseEntity<>(ArticleDTO.from(newArticle), HttpStatus.ACCEPTED);
    }

    @ApiOperation(
            value = "Delete article",
            httpMethod = "DELETE",
            response = String.class)
    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<String> deleteArticle(
            @ApiParam(value = "Article Id", required = true) @PathVariable("id") String id) {
        Article article = this.articleService.getArticle(id);
        if (article == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.articleService.removeArticle(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
