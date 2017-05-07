package hu.bme.mit.textmine.mongo.dictionary.model;

import org.springframework.data.mongodb.core.index.TextIndexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryExample {

    @TextIndexed
    private String exampleSentence;
    private int page;
}
