package hu.bme.mit.textmine.mongo.dictionary.model;

import java.util.List;

import org.springframework.data.mongodb.core.index.TextIndexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inflection {

    @TextIndexed(weight = 10)
    private String name;
    private Integer occurrences;
    private List<EntryExample> examples;
}
