package hu.bme.mit.textmine.rdf.model;

import org.eclipse.rdf4j.model.Value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Triple {

    private String subject;
    private Value predicate;
    private Object object;

    public static Triple of(String s, Value p, Object o) {
        return Triple.builder().subject(s).predicate(p).object(o).build();
    }
}
