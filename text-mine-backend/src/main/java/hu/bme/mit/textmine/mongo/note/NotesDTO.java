package hu.bme.mit.textmine.mongo.note;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class NotesDTO {
    
    @XmlElement(name = "chapter")
    List<ChapterDTO> chapters;
    
    @XmlElement(name = "note")
    List<NoteDTO> notes;
}
