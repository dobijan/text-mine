package hu.bme.mit.textmine.mongo.document.dal;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.google.common.collect.Sets;

import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.document.model.AttachmentDTO;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.QDocument;
import lombok.NonNull;

@Lazy
public interface DocumentRepository
        extends CustomDocumentRepository, MongoRepository<Document, String>, QuerydslPredicateExecutor<Document> {

    public List<Document> findAll();

    public default Set<Document> findAllByIds(List<String> ids) {
        Set<Document> docs = Sets.newHashSet();
        this.findAllById(ids).forEach(docs::add);
        return docs;
    }

    @Query(value = "{ 'corpus.$id' : ?0 }")
    public List<Document> findByCorpusId(ObjectId id);

    public default boolean exists(String id) {
        return this.exists(new QDocument("document").id.eq(new ObjectId(id)));
    }

    public List<Document> findByTitle(String title);

    public Page<Document> findBy(TextCriteria criteria, Pageable page);

    public List<Document> findAllByOrderByScoreDesc(TextCriteria criteria);

    public default Set<Document> languageAgnosticQuery(List<String> phrases) {
        Set<Document> docs = Sets.newHashSet();
        for (String phrase : phrases) {
            docs.addAll(this.findAllByOrderByScoreDesc(TextCriteria.forLanguage("none").caseSensitive(true)
                    .diacriticSensitive(true).matchingPhrase(phrase)));

        }
        return docs;
    }

    public default List<Document> queryWithParams(String entryText, List<PartOfSpeech> partsOfSpeech,
            List<String> entryWords, List<String> documentIds, Integer page, Integer size) {
        List<ObjectId> allowedIds = null;
        if (entryText != null) {
            allowedIds = this.findAllByOrderByScoreDesc(
                    TextCriteria.forLanguage("none")
                            .caseSensitive(true)
                            .diacriticSensitive(true)
                            .matchingPhrase(entryText))
                    .stream().map(d -> d.getId().toString())
                    .filter(id -> documentIds == null ? true : documentIds.contains(id))
                    .map(ObjectId::new)
                    .collect(Collectors.toList());
        } else if (documentIds != null) {
            allowedIds = documentIds.stream().map(ObjectId::new).collect(Collectors.toList());
        }
        if (allowedIds != null && allowedIds.isEmpty()) {
            return Lists.newArrayList();
        }
        if (entryWords != null) {
            List<ObjectId> articleRelatedDocIds = this.getArticlesByEntryWords(entryWords).stream()
                    .filter(a -> {
                        if (partsOfSpeech != null) {
                            List<PartOfSpeech> pos = Lists.newArrayList(a.getPartOfSpeech());
                            pos.retainAll(partsOfSpeech);
                            return !pos.isEmpty();
                        } else {
                            return true;
                        }
                    })
                    .map(a -> new ObjectId(a.getDocumentId()))
                    .collect(Collectors.toList());
            if (allowedIds == null) {
                allowedIds = articleRelatedDocIds;
            } else {
                allowedIds.retainAll(articleRelatedDocIds);
            }
        } else if (partsOfSpeech != null) {
            List<ObjectId> articleRelatedDocIds = this.getArticlesByPartsOfSpeech(partsOfSpeech).stream()
                    .map(a -> new ObjectId(a.getDocumentId()))
                    .collect(Collectors.toList());
            if (allowedIds == null) {
                allowedIds = articleRelatedDocIds;
            } else {
                allowedIds.retainAll(articleRelatedDocIds);
            }
        }
        if (allowedIds == null) {
            return this.findAll(PageRequest.of(page == null ? 0 : page, size == null || size == 0 ? 100 : size))
                    .getContent();
        } else {
            return this.findAll(QDocument.document.id.in(allowedIds),
                    PageRequest.of(page == null ? 0 : page, size == null || size == 0 ? 100 : size))
                    .getContent();
        }
    }

    public default String uploadAttachmentForDocument(@NonNull String documentId, @NonNull AttachmentDTO attachment) {
        Document doc = this.findById(documentId).orElse(null);
        if (doc == null) {
            return null;
        }
        String attachmentId = this.uploadAttachment(attachment);
        if (attachmentId == null) {
            return null;
        }
        if (doc.getAttachmentIds() == null) {
            doc.setAttachmentIds(Lists.newArrayList());
        }
        doc.getAttachmentIds().add(attachmentId);
        this.save(doc);
        return attachmentId;
    }

    public default List<GridFsResource> getDocumentAttachments(@NonNull String documentId) {
        Document doc = this.findById(documentId).orElse(null);
        if (doc == null) {
            return null;
        }
        List<GridFsResource> attachments = Lists.newArrayList();
        for (String attachmentId : doc.getAttachmentIds()) {
            attachments.add(this.getAttachment(attachmentId));
        }
        return attachments;
    }

    public default void deleteDocumentAttachments(@NonNull String documentId) {
        Document doc = this.findById(documentId).orElse(null);
        if (doc == null) {
            return;
        }
        if (doc.getAttachmentIds() == null) {
            doc.setAttachmentIds(Lists.newArrayList());
        }
        for (String attachmentId : doc.getAttachmentIds()) {
            this.deleteAttachment(attachmentId);
        }
        doc.getAttachmentIds().clear();
        this.save(doc);
    }
}
