package hu.bme.mit.textmine.rdf.model;

import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;

import hu.bme.mit.textmine.mongo.core.RdfEntity;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Location extends RdfEntity {

    private String name;

    @Override
    public String getHash() {
        return Hashing.sha256()
                .hashString(String.join(";", "location", this.name), StandardCharsets.UTF_8)
                .toString();
    }

    @Override
    public String getResourcePostfix(TextMineVocabularyService vocabulary) {
        return vocabulary.getLocationResourcePostfix();
    }

}
