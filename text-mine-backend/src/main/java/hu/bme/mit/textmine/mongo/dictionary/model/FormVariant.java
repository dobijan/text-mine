package hu.bme.mit.textmine.mongo.dictionary.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormVariant {
    private String name;
    private List<Inflection> inflections;
}
