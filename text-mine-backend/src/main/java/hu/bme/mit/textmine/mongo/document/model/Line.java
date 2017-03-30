package hu.bme.mit.textmine.mongo.document.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Line {

    private int    serial;
    private String content;
}
