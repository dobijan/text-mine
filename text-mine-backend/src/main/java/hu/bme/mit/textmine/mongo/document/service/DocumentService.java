package hu.bme.mit.textmine.mongo.document.service;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.bme.mit.textmine.mongo.document.dal.DocumentRepository;
import hu.bme.mit.textmine.mongo.document.model.Document;
import hu.bme.mit.textmine.mongo.document.model.DocumentFileDTO;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository repository;
    
    public List<Document> getAllDocuments() {
        return this.repository.findAll();
    }
   
    public Document getDocument(String id) {
        return this.repository.findOne(id);
    }
    
    public List<Document> getDocumentsByCorpus(String id) {
        return this.repository.findByCorpusId(new ObjectId(id));
    }
    
    public Document createDocument (DocumentFileDTO dto) {
//        Document newDocument = this.repository.insert(document);
//        return newDocument.getId().toString();
        return null;
    }
    
    public Document updateDocument (Document document) {
        Document oldDocument = this.repository.findOne(document.getId().toString());
        if (oldDocument != null) {
            oldDocument.setAuthor(document.getAuthor());
            oldDocument.setContent(document.getContent());
            oldDocument.setCorpus(document.getCorpus());
            oldDocument.setTitle(document.getTitle());
            oldDocument.setSections(document.getSections());
            this.repository.save(oldDocument);
            return oldDocument;
        }
        return null;
    }
    
    public void removeDocument (Document document) {
        this.repository.delete(document);
    }
    
    public void removeDocuments(List<Document> documents) {
        this.repository.delete(documents);
    }
}
