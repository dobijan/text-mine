package hu.bme.mit.textmine.mongo.document.model;

import java.nio.charset.StandardCharsets;

import com.google.common.hash.Hashing;

import hu.bme.mit.textmine.mongo.core.RdfEntity;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class Line extends RdfEntity {

    private int serial;
    private String content;

    @Override
    public String getHash() {
        return Hashing.sha256()
                .hashString(String.join(";", Integer.toString(this.serial), this.content), StandardCharsets.UTF_8)
                .toString();
    }

    @Override
    public String getResourcePostfix(TextMineVocabularyService vocabulary) {
        return vocabulary.getLineResourcePostfix();
    }
}
