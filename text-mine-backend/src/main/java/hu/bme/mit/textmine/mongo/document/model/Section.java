package hu.bme.mit.textmine.mongo.document.model;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.common.hash.Hashing;

import hu.bme.mit.textmine.mongo.core.RdfEntity;
import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Section extends RdfEntity {

    private int serial;
    private String content;
    private List<Line> lines;

    @Override
    public String getHash() {
        return Hashing.sha256()
                .hashString(String.join(";", Integer.toString(this.serial), this.content), StandardCharsets.UTF_8)
                .toString();
    }

    @Override
    public String getResourcePostfix(TextMineVocabularyService vocabulary) {
        return vocabulary.getSectionResourcePostfix();
    }
}
