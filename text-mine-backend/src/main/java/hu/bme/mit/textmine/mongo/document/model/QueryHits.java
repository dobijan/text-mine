package hu.bme.mit.textmine.mongo.document.model;

import java.util.Set;

import hu.bme.mit.textmine.mongo.core.RdfEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryHits<T extends RdfEntity> {

    private Set<T> baseHits;
    private Set<T> noteHits;

    public boolean isEmpty() {
        return this.baseHits.isEmpty() && this.noteHits.isEmpty();
    }
}
