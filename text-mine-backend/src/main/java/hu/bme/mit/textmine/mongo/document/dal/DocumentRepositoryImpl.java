package hu.bme.mit.textmine.mongo.document.dal;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;

import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.Section;

@Repository
public class DocumentRepositoryImpl implements CustomDocumentRepository {

    @Autowired
    private MongoTemplate template;

    private AggregationResults<Section> aggregateBySection(String documentId, String arrayname,
            Criteria sectionCriteria) {
        List<AggregationOperation> aggregations = Lists.newArrayList();
        aggregations.add(Aggregation.match(Criteria.where("_id").is(new ObjectId(documentId))));
        aggregations.add(Aggregation.project(arrayname));
        aggregations.add(Aggregation.unwind(arrayname));
        if (sectionCriteria != null) {
            aggregations.add(Aggregation.match(sectionCriteria));
        }
        aggregations.add(Aggregation.group(Fields.from(Fields.field("iri", String.join(".", arrayname, "iri")),
                Fields.field("serial", String.join(".", arrayname, "serial")),
                Fields.field("content", String.join(".", arrayname, "content")))));
        aggregations.add(Aggregation.project("iri", "serial", "content"));
        TypedAggregation<Document> ta = Aggregation.newAggregation(Document.class, aggregations);
        return template.aggregate(ta, Document.class, Section.class);
    }

    private AggregationResults<Line> aggregateByLine(String documentId, String arrayname, Criteria sectionCriteria,
            Criteria lineCriteria) {
        List<AggregationOperation> aggregations = Lists.newArrayList();
        aggregations.add(Aggregation.match(Criteria.where("_id").is(new ObjectId(documentId))));
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
    public List<Line> getLinesByIri(String documentId, List<String> iris) {
        return this.aggregateByLine(documentId, "pages", null, Criteria.where("pages.lines.iri").in(iris))
                .getMappedResults();
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
    public List<Section> getPagesByKeyword(String documentId, String keyword) {
        return this.aggregateBySection(documentId, "pages", Criteria.where("pages.content").regex(keyword))
                .getMappedResults();
    }
}
