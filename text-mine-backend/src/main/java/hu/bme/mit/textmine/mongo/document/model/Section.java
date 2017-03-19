package hu.bme.mit.textmine.mongo.document.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Section {

    private int       serial;
    private String     content;
    private List<Line> lines;
}
