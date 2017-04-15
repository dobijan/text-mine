package hu.bme.mit.textmine.mongo.note;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "chapter")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChapterDTO {
    
    @XmlAttribute(namespace = "xml", name = "id")
    public String id;
    
    @XmlAttribute(name = "number")
    public String number;
}
