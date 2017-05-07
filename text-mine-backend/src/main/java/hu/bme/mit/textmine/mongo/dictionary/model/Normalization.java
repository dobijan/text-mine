package hu.bme.mit.textmine.mongo.dictionary.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Normalization {

    private int startIndex;
    private int endIndex;
    private String replacement;
}
