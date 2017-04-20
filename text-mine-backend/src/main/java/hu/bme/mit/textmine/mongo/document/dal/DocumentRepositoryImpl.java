package hu.bme.mit.textmine.mongo.document.dal;

import java.util.List;

import org.assertj.core.util.Lists;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.Line;

@Repository
public class DocumentRepositoryImpl implements CustomDocumentRepository {

    @Autowired
    private MongoTemplate template;

    @Override
    public List<Line> getDocumentLinesByIRI(String documentId, List<String> iris) {
        List<AggregationOperation> aggregations = Lists.newArrayList();
        aggregations.add(Aggregation.match(Criteria.where("_id").is(new ObjectId(documentId))));
        aggregations.add(Aggregation.project("pages"));
        aggregations.add(Aggregation.unwind("pages"));
        aggregations.add(Aggregation.unwind("pages.lines"));
        aggregations.add(Aggregation.match(Criteria.where("pages.lines.iri").in(iris)));
        aggregations.add(Aggregation.group().push("pages.lines.iri").as("iri").push("pages.lines.serial").as("serial")
                .push("pages.lines.content").as("content"));
        aggregations.add(Aggregation.project("iri", "serial", "content"));
        TypedAggregation<Document> ta = Aggregation.newAggregation(Document.class, aggregations);
        return template.aggregate(ta, Document.class, Line.class).getMappedResults();
    }

    @Override
    public Line getLineByPageAndSerial(String documentId, int pageNumber, int serial) {
        List<AggregationOperation> aggregations = Lists.newArrayList();
        aggregations.add(Aggregation.match(Criteria.where("_id").is(new ObjectId(documentId))));
        aggregations.add(Aggregation.project("pages"));
        aggregations.add(Aggregation.unwind("pages"));
        aggregations.add(Aggregation.match(Criteria.where("pages.serial").is(pageNumber)));
        aggregations.add(Aggregation.unwind("pages.lines"));
        aggregations.add(Aggregation.match(Criteria.where("pages.lines.serial").is(serial)));
        aggregations.add(Aggregation.group().push("pages.lines.iri").as("iri").push("pages.lines.serial").as("serial")
                .push("pages.lines.content").as("content"));
        aggregations.add(Aggregation.project("iri", "serial", "content"));
        TypedAggregation<Document> ta = Aggregation.newAggregation(Document.class, aggregations);
        return template.aggregate(ta, Document.class, Line.class).getUniqueMappedResult();
    }

    @Override
    public Line getLineBySectionAndSerial(String documentId, int sectionNumber, int serial) {
        List<AggregationOperation> aggregations = Lists.newArrayList();
        aggregations.add(Aggregation.match(Criteria.where("_id").is(new ObjectId(documentId))));
        aggregations.add(Aggregation.project("sections"));
        aggregations.add(Aggregation.unwind("sections"));
        aggregations.add(Aggregation.match(Criteria.where("sections.serial").is(sectionNumber)));
        aggregations.add(Aggregation.unwind("sections.lines"));
        aggregations.add(Aggregation.match(Criteria.where("sections.lines.serial").is(serial)));
        aggregations.add(Aggregation.group().push("sections.lines.iri").as("iri").push("sections.lines.serial")
                .as("serial").push("sections.lines.content").as("content"));
        aggregations.add(Aggregation.project("iri", "serial", "content"));
        TypedAggregation<Document> ta = Aggregation.newAggregation(Document.class, aggregations);
        return template.aggregate(ta, Document.class, Line.class).getUniqueMappedResult();
    }

    @Override
    public Line getLineByIri(String documentId, String iri) {
        List<AggregationOperation> aggregations = Lists.newArrayList();
        aggregations.add(Aggregation.match(Criteria.where("_id").is(new ObjectId(documentId))));
        aggregations.add(Aggregation.project("sections"));
        aggregations.add(Aggregation.unwind("sections"));
        aggregations.add(Aggregation.unwind("sections.lines"));
        aggregations.add(Aggregation.match(Criteria.where("sections.lines.iri").is(iri)));
        aggregations.add(Aggregation.group().push("sections.lines.iri").as("iri").push("sections.lines.serial")
                .as("serial").push("sections.lines.content").as("content"));
        aggregations.add(Aggregation.project("iri", "serial", "content"));
        TypedAggregation<Document> ta = Aggregation.newAggregation(Document.class, aggregations);
        return template.aggregate(ta, Document.class, Line.class).getUniqueMappedResult();
    }
}
