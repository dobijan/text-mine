package hu.bme.mit.textmine.mongo.dictionary.model;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PartOfSpeechCsvBean {

    private String word;
    private List<PartOfSpeech> partOfSpeechMapped;
    private List<String> partOfSpeech;

    public void setWord(String word) {
        this.word = word;
    }

    private void setPartOfSpeechMapped(List<PartOfSpeech> partOfSpeechMapped) {
        this.partOfSpeechMapped = partOfSpeechMapped;
    }

    public void setPartOfSpeech(List<String> partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
        this.setPartOfSpeechMapped(partOfSpeech.stream().map(PartOfSpeech::of).collect(Collectors.toList()));
    }
}
