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

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/corpora")
public class CorpusController {
    
    @Autowired
    private CorpusService service;
    
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Corpus>> getAll() {
        List<Corpus> corpora = this.service.getAllCorpora();
        if (corpora == null || corpora.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(corpora, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<Corpus> getOne(@PathVariable("id") String id) {
        Corpus corpus = this.service.getCorpus(id);
        if (corpus == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(corpus, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Corpus> post(@RequestBody Corpus corpus) {
       Corpus newCorpus = this.service.createCorpus(corpus);
       if (newCorpus != null) {           
           return new ResponseEntity<>(newCorpus, HttpStatus.CREATED);
       }
       return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity<Corpus> put(@RequestBody Corpus corpus, @PathVariable("id") String id) {
        Corpus oldCorpus = this.service.getCorpus(id);
        if (oldCorpus == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Corpus newCorpus = this.service.updateCorpus(corpus);
        return new ResponseEntity<>(newCorpus, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") String id) {
        Corpus corpus = this.service.getCorpus(id);
        if (corpus == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.service.removeCorpus(corpus);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
