package hu.bme.mit.textmine.mongo.note.dal;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.google.common.collect.Lists;

import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.mongo.note.model.QNote;

public interface NoteRepository
        extends MongoRepository<Note, String>, CustomNoteRepository, QueryDslPredicateExecutor<Note> {

    public default boolean exists(String id) {
        return this.exists(new QNote("note").id.eq(new ObjectId(id)));
    }

    public default List<Note> findByDocumentId(String id) {
        List<Note> result = Lists.newArrayList();
        this.findAll(new QNote("note").document.id.eq(new ObjectId(id))).forEach(result::add);
        return result;
    }

    public List<Note> findByQuote(String quote);

    public Page<Note> findBy(TextCriteria criteria, Pageable page);

    public List<Note> findAllByOrderByScoreDesc(TextCriteria criteria);

    public default List<Note> languageAgnosticQuery(String word) {
        return this.findAllByOrderByScoreDesc(
                TextCriteria.forLanguage("none").caseSensitive(false).diacriticSensitive(false).matching(word));
    }
}
