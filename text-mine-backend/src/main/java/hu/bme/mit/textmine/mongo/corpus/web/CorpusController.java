package hu.bme.mit.textmine.mongo.corpus.web;

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

import hu.bme.mit.textmine.mongo.corpus.model.Corpus;
import hu.bme.mit.textmine.mongo.corpus.service.CorpusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "/corpora")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/corpora")
public class CorpusController {

    @Autowired
    private CorpusService service;

    @ApiOperation(value = "Get corpora", httpMethod = "GET", response = Corpus.class, responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Corpus>> getAll() {
        List<Corpus> corpora = this.service.getAllCorpora();
        if (corpora == null || corpora.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(corpora, HttpStatus.OK);
    }

    @ApiOperation(value = "Get corpus by id", httpMethod = "GET", response = Corpus.class)
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<Corpus> getOne(
            @ApiParam(value = "Id of corpus", required = true) @PathVariable("id") String id) {
        Corpus corpus = this.service.getCorpus(id);
        if (corpus == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(corpus, HttpStatus.OK);
    }

    @ApiOperation(value = "Create corpus", httpMethod = "POST", response = Corpus.class)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Corpus> post(@ApiParam(value = "Corpus", required = true) @RequestBody Corpus corpus) {
        Corpus newCorpus = this.service.createCorpus(corpus);
        if (newCorpus != null) {
            return new ResponseEntity<>(newCorpus, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(value = "Update corpus", httpMethod = "PUT", response = Corpus.class)
    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity<Corpus> put(@ApiParam(value = "Corpus", required = true) @RequestBody Corpus corpus,
            @ApiParam(value = "Corpus id", required = true) @PathVariable("id") String id) {
        Corpus oldCorpus = this.service.getCorpus(id);
        if (oldCorpus == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Corpus newCorpus = this.service.updateCorpus(corpus);
        return new ResponseEntity<>(newCorpus, HttpStatus.OK);
    }

    @ApiOperation(value = "Delete corpus", httpMethod = "DELETE", response = String.class)
    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<String> delete(
            @ApiParam(value = "Corpus id", required = true) @PathVariable("id") String id) {
        Corpus corpus = this.service.getCorpus(id);
        if (corpus == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.service.removeCorpus(corpus);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
