package hu.bme.mit.textmine.mongo.note.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteDTO {

    private String quote;

    private String type;

    private String subType;

    private String content;

    private Integer section;

    private Set<Integer> lineRefs;

    private String documentId;
}
