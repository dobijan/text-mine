package hu.bme.mit.textmine.mongo.dictionary.dal;

import java.util.List;

import hu.bme.mit.textmine.mongo.dictionary.model.DocumentArticles;
import hu.bme.mit.textmine.mongo.dictionary.model.MatchingStrategy;
import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;
import hu.bme.mit.textmine.mongo.document.model.Section;

interface CustomArticleRepository {

    public boolean updatePOS(String entryWord, List<PartOfSpeech> pos);

    public Section getDocumentSection(String documentId, Integer sectionNumber);

    public Section getDocumentPage(String documentId, Integer sectionNumber);

    public List<DocumentArticles> getArticlesByDocumentWithParams(String entryWord, String formVariant,
            String inflection, List<PartOfSpeech> partOfSpeech, MatchingStrategy matchingStrategy, Integer posCount);
}
