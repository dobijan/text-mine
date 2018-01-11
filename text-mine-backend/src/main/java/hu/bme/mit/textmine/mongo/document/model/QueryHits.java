package hu.bme.mit.textmine.mongo.document.model;

import java.util.Set;

import hu.bme.mit.textmine.mongo.core.RdfEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryHits<T extends RdfEntity> {

    private Set<T> baseHits;
    private Set<T> noteHits;

    public boolean isEmpty() {
        return this.baseHits.isEmpty() && this.noteHits.isEmpty();
    }
}
