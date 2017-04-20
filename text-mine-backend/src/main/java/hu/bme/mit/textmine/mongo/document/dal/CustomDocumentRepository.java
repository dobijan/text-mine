package hu.bme.mit.textmine.mongo.document.dal;

import java.util.List;

import hu.bme.mit.textmine.mongo.document.model.Line;

public interface CustomDocumentRepository {

    public List<Line> getDocumentLinesByIRI(String documentId, List<String> iris);

    public Line getLineByPageAndSerial(String documentId, int pageNumber, int serial);

    public Line getLineBySectionAndSerial(String documentId, int sectionNumber, int serial);

    public Line getLineByIri(String documentId, String iri);
}
