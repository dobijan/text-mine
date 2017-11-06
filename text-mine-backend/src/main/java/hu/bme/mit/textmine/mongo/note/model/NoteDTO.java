package hu.bme.mit.textmine.mongo.note.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "note")
@XmlAccessorType(XmlAccessType.FIELD)
public class NoteDTO {
    
    @XmlElement(name = "quote")
    public String quote;
    
    @XmlAttribute
    public String type;
    
    @XmlAttribute
    public String subType;
    
    @XmlValue
    public String content;
}
