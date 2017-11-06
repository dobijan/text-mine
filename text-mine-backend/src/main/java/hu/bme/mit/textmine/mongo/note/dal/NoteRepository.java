package hu.bme.mit.textmine.mongo.note.dal;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.querydsl.core.types.dsl.BooleanExpression;

import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.mongo.note.model.QNote;

@Lazy
public interface NoteRepository
        extends MongoRepository<Note, String>, CustomNoteRepository, QuerydslPredicateExecutor<Note> {

    public static final Logger log = LoggerFactory.getLogger("NoteRepositoryIF");

    public default Iterable<Note> findAllByIds(Collection<String> ids) {
        Iterable<Note> notes;
        log.info("Finding notes by id from mongo. Number of notes: " + ids.size());
        // this.findAll(new QNote("note").id.in(ids.stream().map(ObjectId::new).collect(Collectors.toList())))
        // .forEach(notes::add);
        notes = this.findAllById(ids);
        log.info("Found notes by id from mongo.");
        return notes;
    }

    public default boolean exists(String id) {
        return this.exists(new QNote("note").id.eq(new ObjectId(id)));
    }

    public default List<Note> findByDocumentId(String id) {
        List<Note> result = Lists.newArrayList();
        this.findAll(new QNote("note").documentId.eq(id)).forEach(result::add);
        return result;
    }

    public default List<Note> findByDocumentIdAndIris(String documentId, List<String> iris) {
        List<Note> result = Lists.newArrayList();
        QNote qnote = new QNote("note");
        BooleanExpression exp = qnote.iri.in(iris);
        if (documentId != null) {
            exp = exp.and(qnote.documentId.eq(documentId));
        }
        this.findAll(exp).forEach(result::add);
        return result;
    }

    public List<Note> findByQuote(String quote);

    public Page<Note> findBy(TextCriteria criteria, Pageable page);

    public List<Note> findAllByOrderByScoreDesc(TextCriteria criteria);

    public default Set<Note> languageAgnosticQuery(List<String> phrases) {
        Set<Note> notes = Sets.newHashSet();
        for (String phrase : phrases) {
            MongoRepositoryLogHolder.logger.info("Full text query for: " + phrase);
            notes.addAll(this.findAllByOrderByScoreDesc(TextCriteria.forLanguage("none").caseSensitive(true)
                    .diacriticSensitive(true).matchingPhrase(phrase)));
            MongoRepositoryLogHolder.logger.info("Note count so far:  " + notes.size());
        }
        return notes;
    }
}

final class MongoRepositoryLogHolder {

    static final Logger logger = LoggerFactory.getLogger(MongoRepository.class);
}
