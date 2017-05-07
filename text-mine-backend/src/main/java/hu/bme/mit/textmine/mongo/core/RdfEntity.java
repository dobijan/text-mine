package hu.bme.mit.textmine.mongo.core;

import javax.validation.constraints.NotNull;

import hu.bme.mit.textmine.rdf.service.TextMineVocabularyService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class RdfEntity implements RdfIdentifiable {

    @NotNull(message = "Resource IRI must not be null!")
    protected String iri;

    public abstract String getResourcePostfix(TextMineVocabularyService vocabulary);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((iri == null) ? 0 : iri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RdfEntity other = (RdfEntity) obj;
        if (iri == null) {
            if (other.iri != null)
                return false;
        } else if (!iri.equals(other.iri))
            return false;
        return true;
    }
}
