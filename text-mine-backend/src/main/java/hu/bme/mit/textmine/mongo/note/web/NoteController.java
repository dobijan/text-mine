package hu.bme.mit.textmine.mongo.note.web;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.mongo.note.model.NoteDTO;
import hu.bme.mit.textmine.mongo.note.model.NoteFileDTO;
import hu.bme.mit.textmine.mongo.note.service.NoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "/notes")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, path = "/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    @Lazy
    private DocumentService documentService;

    @ApiOperation(
            value = "Get all notes",
            httpMethod = "GET",
            response = Note.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Note>> getAll() {
        List<Note> notes = this.noteService.getAllNotes();
        if (notes == null || notes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get note by id",
            httpMethod = "GET",
            response = Note.class)
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public ResponseEntity<Note> getOne(@ApiParam(value = "Note id", required = true) @PathVariable("id") String id) {
        Note note = this.noteService.getNote(id);
        if (note == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(note, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Get note by document id",
            httpMethod = "GET",
            response = Note.class,
            responseContainer = "List")
    @RequestMapping(method = RequestMethod.GET, path = "/by-document/{id}")
    public ResponseEntity<List<Note>> getByDocument(
            @ApiParam(value = "Document id", required = true) @PathVariable("id") String id) {
        List<Note> notes = this.noteService.getNotesByDocument(id);
        if (notes == null || notes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/multiple")
    public ResponseEntity<List<Note>> postFile(MultipartHttpServletRequest request) throws IOException {
        String documentId = request.getParameter("documentId");
        MultipartFile file = request.getFile("file");
        if (Stream.of(documentId, file).anyMatch(Objects::isNull)) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.documentService.exists(documentId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Note> result = this.noteService
                .createNotes(NoteFileDTO.builder().documentId(documentId).file(file).build());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Post a note",
            httpMethod = "POST",
            response = Note.class)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Note> postOne(@ApiParam(value = "Note", required = true) @RequestBody NoteDTO note)
            throws IOException {
        if (Stream.of(note.getDocumentId(), note.getSection(), note.getQuote(), note.getContent())
                .anyMatch(Objects::isNull)) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.documentService.exists(note.getDocumentId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Note result = this.noteService.createNote(note);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Update a note",
            httpMethod = "PUT",
            response = Note.class)
    @RequestMapping(method = RequestMethod.PUT, path = "/{id}")
    public ResponseEntity<Note> put(@ApiParam(value = "Note", required = true) @RequestBody Note note,
            @ApiParam(value = "Note id", required = true) @PathVariable("id") String id) {
        Note oldNote = this.noteService.getNote(id);
        if (oldNote == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Note newNote = this.noteService.updateNote(note);
        return new ResponseEntity<>(newNote, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Delete a note",
            httpMethod = "DELETE",
            response = String.class)
    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<String> delete(@ApiParam(value = "Note id", required = true) @PathVariable("id") String id) {
        Note note = this.noteService.getNote(id);
        if (note == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        this.noteService.removeNote(note);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
