package hu.bme.mit.textmine.mongo.dictionary.dal;

import hu.bme.mit.textmine.mongo.dictionary.model.PartOfSpeech;

public interface CustomArticleRepository {
    public boolean updatePOS (String entryWord, PartOfSpeech pos);
}
