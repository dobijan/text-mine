package hu.bme.mit.textmine.rdf.model;

import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.Statement;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RdfStatementsDTO {
    private Map<String, List<Statement>> resources;
}
