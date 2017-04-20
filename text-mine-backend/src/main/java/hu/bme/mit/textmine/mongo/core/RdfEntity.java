package hu.bme.mit.textmine.mongo.core;

import javax.validation.constraints.NotNull;

import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public abstract class RdfEntity implements RdfIdentifiable {

    @NotNull(message = "Resource IRI must not be null!")
    protected String iri;

    public abstract String getResourcePostfix(TextMineVocabularyService vocabulary);
}
