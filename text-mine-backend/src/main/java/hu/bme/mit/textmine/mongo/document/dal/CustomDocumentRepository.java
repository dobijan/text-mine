package hu.bme.mit.textmine.mongo.document.dal;

import java.util.List;

import org.springframework.data.mongodb.gridfs.GridFsResource;

import hu.bme.mit.textmine.mongo.dictionary.model.Article;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.document.model.AttachmentDTO;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.Section;

interface CustomDocumentRepository {

    public List<Line> getLinesByIri(String documentId, Integer sectionSerial, List<String> iris);

    public List<Line> getSectionLinesBySerial(String documentId, Integer sectionNumber, List<Long> serials);

    public List<Line> getPageLinesBySerial(String documentId, int sectionNumber, List<Long> serials);

    public Line getLineByPageAndSerial(String documentId, int pageNumber, int serial);

    public Line getLineBySectionAndSerial(String documentId, int sectionNumber, int serial);

    public Line getLineByIri(String documentId, String iri);

    public List<Line> getLinesByKeyword(String documentId, int sectionNumber, String keyword);

    public List<Section> getSectionsByIri(String documentId, List<String> iris);

    public List<Section> getSectionsBySerial(String documentId, List<Long> serials);

    public Section getSectionBySerial(String documentId, int serial);

    public Section getSectionByIri(String documentId, String iri);

    public List<Section> getSectionsByKeyword(String documentId, String keyword);

    public List<Section> getPagesByIri(String documentId, List<String> iris);

    public List<Section> getPagesBySerial(String documentId, List<Long> serials);

    public Section getPageBySerial(String documentId, int serial);

    public Section getPageByIri(String documentId, String iri);

    public List<Section> getPagesByKeyword(String documentId, String keyword);

    public List<Article> getArticlesByEntryWords(List<String> entryWords);

    public List<Article> getArticlesByPartsOfSpeech(List<PartOfSpeech> pos);

    public String uploadAttachment(AttachmentDTO attachment);

    public void deleteAttachment(String attachmentId);

    public GridFsResource getAttachment(String attachmentId);
}
