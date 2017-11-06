package hu.bme.mit.textmine.mongo.note.service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import hu.bme.mit.textmine.mongo.core.SearchStrategy;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.service.DocumentService;
import hu.bme.mit.textmine.mongo.note.dal.NoteRepository;
import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.mongo.note.model.NoteFileDTO;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import hu.bme.mit.textmine.solr.dal.note.SolrNoteRepository;
import hu.bme.mit.textmine.solr.model.SolrNote;
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

    @Value("${search.strategy}")
    private SearchStrategy searchStrategy;

    @Autowired
    @Lazy
    private NoteRepository repository;

    @Autowired
    @Lazy
    private DocumentService documentService;

    @Autowired
    private TextMineVocabularyService vocabulary;

    @Autowired
    private SolrNoteRepository solrNoteRepository;

    public List<Note> getAllNotes() {
        return this.repository.findAll();
    }

    public Note getNote(String id) {
        return this.repository.findById(id).orElse(null);
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

    public List<Note> getNotesByIriAndDocumentId(String documentId, List<String> iris) {
        return this.repository.findByDocumentIdAndIris(documentId, iris);
    }

    public Iterable<Note> languageAgnosticFullTextQuery(List<String> phrases, boolean exact) {
        if (SearchStrategy.MONGO.equals(this.searchStrategy)) {
            return this.repository.languageAgnosticQuery(phrases);
        } else {
            List<SolrNote> solrNotes = this.solrNoteRepository.phraseQuery(Optional.empty(), Optional.empty(), phrases,
                    exact, true);
            return this.repository.findAllByIds(solrNotes.stream().map(SolrNote::getId).collect(Collectors.toList()));
        }
    }

    public boolean exists(String id) {
        return this.repository.exists(id);
    }

    @SneakyThrows(IOException.class)
    public List<Note> createNotes(NoteFileDTO dto) {
        Document document = documentService.getDocument(dto.getDocumentId());
        String content = new String(dto.getFile().getBytes(), StandardCharsets.UTF_8);
        List<Note> notes = this.parseNotesWithStaX(content, document);
        this.repository.saveAll(notes);
        List<SolrNote> solrNotes = Lists.newArrayList();
        for (Note note : notes) {
            solrNotes.add(this.createSolrNote(note));
        }
        this.solrNoteRepository.saveAll(solrNotes);
        return notes;
    }

    public Note updateNote(Note note) {
        this.repository.save(note);
        this.solrNoteRepository.deleteById(note.getId().toString());
        this.solrNoteRepository.save(this.createSolrNote(note));
        return note;
    }

    public void removeNote(Note note) {
        this.repository.delete(note);
        this.solrNoteRepository.deleteById(note.getId().toHexString().toString());
    }

    public void removeNotes(List<Note> notes) {
        this.repository.deleteAll(notes);
        this.solrNoteRepository.deleteAll(this.solrNoteRepository
                .findAllById(notes.stream().map(Note::getId).map(ObjectId::toString).collect(Collectors.toList())));
    }

    private SolrNote createSolrNote(Note note) {
        return SolrNote.builder()
                .content(note.getContent())
                .documentId(note.getDocumentId())
                .id(note.getId().toString())
                .lineRefs(note.getLineRefs().stream().map(Long::new).collect(Collectors.toList()))
                .quote(note.getQuote())
                .section(new Long(note.getSection()))
                .subType(note.getSubType())
                .type(note.getType())
                .build();
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
                    Note note = Note.builder().content(noteContent).documentId(document.getId().toString())
                            .lineRefs(lineRefs).quote(quote)
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
