package hu.bme.mit.textmine.mongo.note.service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import hu.bme.mit.textmine.mongo.note.dal.NoteRepository;
import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.mongo.note.model.NoteFileDTO;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.SneakyThrows;

@Service
public class NoteService {

    private static final String CHAPTER = "chapter";

    private static final String NOTE = "note";

    private static final String QUOTE = "quote";

    private static final String NUMBER = "number";

    private static final String TYPE = "type";

    private static final String SUBTYPE = "subtype";

    private static final String LINE_REFS = "cref";

    private static final Pattern rangePattern = Pattern.compile("(\\d+)-(\\d+)");

    private static final Pattern rangesPattern = Pattern.compile("(\\d+-\\d+,\\s+)+\\d+-\\d+");

    @Autowired
    private NoteRepository repository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private TextMineVocabularyService vocabulary;

    public List<Note> getAllNotes() {
        return this.repository.findAll();
    }

    public Note getNote(String id) {
        return this.repository.findOne(id);
    }

    public List<Note> getNotesByDocument(String documentId) {
        Document document = this.documentService.getDocument(documentId);
        if (document == null) {
            return null;
        }
        return this.repository.findByDocumentId(documentId);
    }

    public List<Note> getNotesByQuote(String quote) {
        return this.repository.findByQuote(quote);
    }

    public List<Note> languageAgnosticFullTextQuery(String word) {
        return this.repository.languageAgnosticQuery(word);
    }

    public boolean exists(String id) {
        return this.repository.exists(id);
    }

    @SneakyThrows(IOException.class)
    public List<Note> createNotes(NoteFileDTO dto) {
        Document document = documentService.getDocument(dto.getDocumentId());
        String content = new String(dto.getFile().getBytes(), StandardCharsets.UTF_8);
        List<Note> notes = this.parseNotesWithStaX(content, document);
        this.repository.insert(notes);
        return notes;
    }

    public Note updateNote(Note note) {
        this.repository.save(note);
        return note;
    }

    public void removeNote(Note note) {
        this.repository.delete(note);
    }

    public void removeNotes(List<Note> note) {
        this.repository.delete(note);
    }

    @SneakyThrows(XMLStreamException.class)
    private List<Note> parseNotesWithStaX(String content, Document document) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader = factory.createXMLEventReader(new StringReader(content));
        List<Note> notes = Lists.newArrayList();
        int chapter = 0;
        boolean inNote = false;
        boolean inQuote = false;
        String type = "";
        String subtype = "";
        String quote = "";
        String noteContent = "";
        Set<Integer> lineRefs = null;
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                StartElement e = event.asStartElement();
                if (e.getName().getLocalPart().equals(CHAPTER)) {
                    chapter = Integer.parseInt(e.getAttributeByName(new QName(NUMBER)).getValue());
                } else if (e.getName().getLocalPart().equals(NOTE)) {
                    inNote = true;
                    type = e.getAttributeByName(new QName(TYPE)).getValue();
                    subtype = e.getAttributeByName(new QName(SUBTYPE)).getValue();
                } else if (e.getName().getLocalPart().equals(QUOTE)) {
                    inQuote = true;
                    String refString = e.getAttributeByName(new QName(LINE_REFS)).getValue();
                    Matcher rangeMatcher = rangePattern.matcher(refString);
                    Matcher rangesMatcher = rangesPattern.matcher(refString);
                    if (rangeMatcher.matches()) {
                        Set<Integer> rangeBoundaries = Sets.newHashSet(Integer.parseInt(rangeMatcher.group(1)),
                                Integer.parseInt(rangeMatcher.group(2)));
                        lineRefs = ContiguousSet.create(Range.closed(Collections.min(rangeBoundaries).intValue(),
                                Collections.max(rangeBoundaries).intValue()), DiscreteDomain.integers());
                    } else if (refString.isEmpty() || refString.trim().startsWith(",")
                            || refString.trim().endsWith(",")) {
                        lineRefs = Sets.newHashSet();
                    } else if (rangesMatcher.matches()) {
                        String[] ranges = refString.split(",\\s+");
                        lineRefs = Sets.newHashSet();
                        for (String range : ranges) {
                            Matcher subrangeMatcher = rangePattern.matcher(range);
                            if (subrangeMatcher.matches()) {
                                Set<Integer> rangeBoundaries = Sets.newHashSet(
                                        Integer.parseInt(subrangeMatcher.group(1)),
                                        Integer.parseInt(subrangeMatcher.group(2)));
                                lineRefs.addAll(ContiguousSet.create(
                                        Range.closed(Collections.min(rangeBoundaries).intValue(),
                                                Collections.max(rangeBoundaries).intValue()),
                                        DiscreteDomain.integers()));
                            } else {
                                lineRefs.add(Integer.parseInt(range));
                            }
                        }
                    } else {
                        lineRefs = Sets.newHashSet(Integer.parseInt(refString));
                    }
                }
            } else if (event.isEndElement()) {
                EndElement e = event.asEndElement();
                if (e.getName().getLocalPart().equals(NOTE)) {
                    inNote = false;
                    Note note = Note.builder().content(noteContent).document(document).lineRefs(lineRefs).quote(quote)
                            .subType(subtype).type(type).section(chapter).build();
                    note.setIri(this.vocabulary.asResource(note));
                    notes.add(note);
                    noteContent = "";
                } else if (e.getName().getLocalPart().equals(QUOTE)) {
                    inQuote = false;
                }
            } else if (event.isCharacters()) {
                if (inQuote) {
                    quote = event.asCharacters().getData();
                } else if (inNote) {
                    String data = event.asCharacters().getData();
                    if (!data.trim().isEmpty()) {
                        noteContent += data;
                    }
                }
            }
        }
        return notes;
    }
}
