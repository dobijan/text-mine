package hu.bme.mit.textmine.mongo.document.dal;

import java.io.IOException;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.dictionary.service.ArticleService;
import hu.bme.mit.textmine.mongo.document.model.AttachmentDTO;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.Section;
import lombok.NonNull;
import lombok.SneakyThrows;

@Repository
public class DocumentRepositoryImpl implements CustomDocumentRepository {

    @Autowired
    @Lazy
    private MongoTemplate template;

    @Autowired
    private ArticleService articleService;

    @Autowired
    @Lazy
    private GridFsTemplate attachmentTemplate;

    private AggregationResults<Section> aggregateBySection(String documentId, String arrayname,
            Criteria sectionCriteria) {
        List<AggregationOperation> aggregations = Lists.newArrayList();
        if (documentId != null) {
            aggregations.add(Aggregation.match(Criteria.where("_id").is(new ObjectId(documentId))));
        }
        aggregations.add(Aggregation.project(arrayname));
        aggregations.add(Aggregation.unwind(arrayname));
        if (sectionCriteria != null) {
            aggregations.add(Aggregation.match(sectionCriteria));
        }
        aggregations.add(Aggregation.group(Fields.from(Fields.field("iri", String.join(".", arrayname, "iri")),
                Fields.field("serial", String.join(".", arrayname, "serial")),
                Fields.field("lines", String.join(".", arrayname, "lines")),
                Fields.field("normalized", String.join(".", arrayname, "normalized")),
                Fields.field("content", String.join(".", arrayname, "content")))));
        aggregations.add(Aggregation.project("iri", "serial", "content", "normalized", "lines"));
        TypedAggregation<Document> ta = Aggregation.newAggregation(Document.class, aggregations);
        return template.aggregate(ta, Document.class, Section.class);
    }

    private AggregationResults<Line> aggregateByLine(String documentId, String arrayname, Criteria sectionCriteria,
            Criteria lineCriteria) {
        List<AggregationOperation> aggregations = Lists.newArrayList();
        if (documentId != null) {
            aggregations.add(Aggregation.match(Criteria.where("_id").is(new ObjectId(documentId))));
        }
        aggregations.add(Aggregation.project(arrayname));
        aggregations.add(Aggregation.unwind(arrayname));
        if (sectionCriteria != null) {
            aggregations.add(Aggregation.match(sectionCriteria));
        }
        aggregations.add(Aggregation.unwind(String.join(".", arrayname, "lines")));
        if (lineCriteria != null) {
            aggregations.add(Aggregation.match(lineCriteria));
        }
        aggregations.add(Aggregation.group(Fields.from(Fields.field("iri", String.join(".", arrayname, "lines", "iri")),
                Fields.field("serial", String.join(".", arrayname, "lines", "serial")),
                Fields.field("content", String.join(".", arrayname, "lines", "content")))));
        aggregations.add(Aggregation.project("iri", "serial", "content"));
        TypedAggregation<Document> ta = Aggregation.newAggregation(Document.class, aggregations);
        return template.aggregate(ta, Document.class, Line.class);
    }

    @Override
    public List<Line> getLinesByIri(String documentId, Integer sectionSerial, List<String> iris) {
        return this
                .aggregateByLine(documentId, "sections",
                        sectionSerial == null ? null : Criteria.where("sections.serial").is(sectionSerial),
                        Criteria.where("sections.lines.iri").in(iris))
                .getMappedResults();
    }

    @Override
    public List<Line> getSectionLinesBySerial(String documentId, Integer sectionNumber, List<Long> serials) {
        return this.aggregateByLine(documentId, "sections",
                sectionNumber == null ? null : Criteria.where("sections.serial").is(sectionNumber),
                Criteria.where("sections.lines.serial").in(serials)).getMappedResults();
    }

    @Override
    public List<Line> getPageLinesBySerial(String documentId, int sectionNumber, List<Long> serials) {
        return this.aggregateByLine(documentId, "pages", Criteria.where("pages.serial").is(sectionNumber),
                Criteria.where("pages.lines.serial").in(serials)).getMappedResults();
    }

    @Override
    public Line getLineByPageAndSerial(String documentId, int pageNumber, int serial) {
        return this.aggregateByLine(documentId, "pages", Criteria.where("pages.serial").is(pageNumber),
                Criteria.where("pages.lines.serial").is(serial)).getUniqueMappedResult();
    }

    @Override
    public Line getLineBySectionAndSerial(String documentId, int sectionNumber, int serial) {
        return this.aggregateByLine(documentId, "sections", Criteria.where("sections.serial").is(sectionNumber),
                Criteria.where("sections.lines.serial").is(serial)).getUniqueMappedResult();
    }

    @Override
    public Line getLineByIri(String documentId, String iri) {
        return this.aggregateByLine(documentId, "sections", null, Criteria.where("sections.lines.iri").is(iri))
                .getUniqueMappedResult();
    }

    @Override
    public List<Line> getLinesByKeyword(String documentId, int sectionNumber, String keyword) {
        return this.aggregateByLine(documentId, "sections", Criteria.where("sections.serial").is(sectionNumber),
                Criteria.where("sections.lines.content").regex(keyword)).getMappedResults();
    }

    @Override
    public List<Section> getSectionsByIri(String documentId, List<String> iris) {
        return this.aggregateBySection(documentId, "sections", Criteria.where("sections.iri").in(iris))
                .getMappedResults();
    }

    @Override
    public List<Section> getSectionsBySerial(String documentId, List<Long> serials) {
        return this.aggregateBySection(documentId, "sections", Criteria.where("sections.serial").in(serials))
                .getMappedResults();
    }

    @Override
    public Section getSectionBySerial(String documentId, int serial) {
        return this.aggregateBySection(documentId, "sections", Criteria.where("sections.serial").is(serial))
                .getUniqueMappedResult();
    }

    @Override
    public Section getSectionByIri(String documentId, String iri) {
        return this.aggregateBySection(documentId, "sections", Criteria.where("sections.iri").is(iri))
                .getUniqueMappedResult();
    }

    @Override
    public List<Section> getSectionsByKeyword(String documentId, String keyword) {
        return this.aggregateBySection(documentId, "sections", Criteria.where("sections.content").regex(keyword))
                .getMappedResults();
    }

    @Override
    public List<Section> getPagesByIri(String documentId, List<String> iris) {
        return this.aggregateBySection(documentId, "pages", Criteria.where("pages.iri").in(iris)).getMappedResults();
    }

    @Override
    public Section getPageBySerial(String documentId, int serial) {
        return this.aggregateBySection(documentId, "pages", Criteria.where("pages.serial").is(serial))
                .getUniqueMappedResult();
    }

    @Override
    public Section getPageByIri(String documentId, String iri) {
        return this.aggregateBySection(documentId, "pages", Criteria.where("pages.iri").is(iri))
                .getUniqueMappedResult();
    }

    @Override
    public List<Section> getPagesBySerial(String documentId, List<Long> serials) {
        return this.aggregateBySection(documentId, "pages", Criteria.where("pages.serial").in(serials))
                .getMappedResults();
    }

    @Override
    public List<Section> getPagesByKeyword(String documentId, String keyword) {
        return this.aggregateBySection(documentId, "pages", Criteria.where("pages.content").regex(keyword))
                .getMappedResults();
    }

    @Override
    public List<Article> getArticlesByEntryWords(List<String> entryWords) {
        return Lists.newArrayList(this.articleService.languageAgnosticFullTextQuery(entryWords, false));
    }

    @Override
    public List<Article> getArticlesByPartsOfSpeech(List<PartOfSpeech> pos) {
        return this.articleService.getArticlesByPartsOfSpeech(pos);
    }

    @Override
    @SneakyThrows(IOException.class)
    public String uploadAttachment(@NonNull AttachmentDTO attachment) {
        DBObject metadata = new BasicDBObject();
        metadata.put("filename", attachment.getContent().getOriginalFilename());
        metadata.put("name", attachment.getContent().getName());
        for (String key : attachment.getMetadata().keySet()) {
            metadata.put(key, attachment.getMetadata().get(key));
        }
        String attachmentId = this.attachmentTemplate.store(attachment.getContent().getInputStream(),
                attachment.getContent().getOriginalFilename(), attachment.getContent().getContentType(), metadata)
                .toString();
        return attachmentId;
    }

    @Override
    public void deleteAttachment(@NonNull String attachmentId) {
        this.attachmentTemplate.delete(new Query(Criteria.where("_id").is(attachmentId)));
    }

    @Override
    public GridFsResource getAttachment(@NonNull String attachmentId) {
        GridFSFile attachment = this.attachmentTemplate.findOne(new Query(Criteria.where("_id").is(attachmentId)));
        return this.attachmentTemplate.getResource(attachment.getFilename());
    }
}
