package hu.bme.mit.textmine.mongo.dictionary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PartOfSpeechCsvBean {
    private String word;
    private PartOfSpeech partOfSpeechMapped;
    private String partOfSpeech;
    
    public void setWord(String word) {
        this.word = word;
    }
    
    private void setPartOfSpeechMapped(PartOfSpeech partOfSpeechMapped) {
        this.partOfSpeechMapped = partOfSpeechMapped;
    }
    
    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
        this.setPartOfSpeechMapped(PartOfSpeech.of(partOfSpeech));
    }
}
