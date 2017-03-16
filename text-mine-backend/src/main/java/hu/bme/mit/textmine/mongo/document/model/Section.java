package hu.bme.mit.textmine.mongo.document.model;

import java.util.List;

import lombok.Data;

@Data
public class Section {

    private Long       serial;
    private String     content;
    private List<Line> lines;
}
