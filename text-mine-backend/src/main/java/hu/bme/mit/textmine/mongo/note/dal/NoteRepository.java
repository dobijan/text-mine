package hu.bme.mit.textmine.mongo.note.dal;

import java.util.List;

import org.bson.types.ObjectId;
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
}
