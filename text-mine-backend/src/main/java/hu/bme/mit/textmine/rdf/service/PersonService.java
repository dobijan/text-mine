package hu.bme.mit.textmine.rdf.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import hu.bme.mit.textmine.mongo.core.RdfEntity;
import hu.bme.mit.textmine.mongo.document.model.Line;
import hu.bme.mit.textmine.mongo.document.model.QueryHits;
import hu.bme.mit.textmine.mongo.document.model.Section;
import hu.bme.mit.textmine.mongo.note.model.Note;
import hu.bme.mit.textmine.rdf.model.Person;

@Service
public class PersonService extends RdfResourceService {

    @Override
    protected void setRelationType() {
        this.relationType = this.vocabulary.personRelation();
    }

    @Override
    protected void setResourceType() {
        this.resourceType = this.vocabulary.person();
    }

    @Override
    protected String getLabel(RdfEntity entity) {
        return ((Person) entity).getName();
    }

    @Override
    protected RdfEntity createResource(String resource) {
        Person person = Person.builder().name(resource).build();
        person.setIri(this.vocabulary.asResource(person));
        return person;
    }

    public List<String> getIrisForPerson(String personName) {
        return this.irisForLabel(personName);
    }

    public List<Line> linesForPersonIri(String documentId, Integer sectionSerial, List<String> iris) {
        return this.linesForIri(documentId, sectionSerial, iris);
    }

    public List<Section> sectionsForPersonIri(String documentId, List<String> iris) {
        return this.sectionsForIri(documentId, iris);
    }

    public List<Section> pagesForPersonIri(String documentId, List<String> iris) {
        return this.pagesForIri(documentId, iris);
    }

    public List<Note> notesForPersonIri(String documentId, List<String> iris) {
        return this.notesForIri(documentId, iris);
    }

    public QueryHits<Line> linesForPerson(String documentId, Integer sectionSerial, String person) {
        List<String> iris = getIrisForPerson(person);
        if (!iris.isEmpty()) {
            List<Line> baseHits = linesForPersonIri(documentId, sectionSerial, iris);
            List<Note> noteHits = notesForIri(documentId, iris);
            List<Long> lineSerials = noteHits.stream().map(Note::getLineRefs)
                    .flatMap(Set::stream)
                    .map(Long::new)
                    .collect(Collectors.toList());
            return QueryHits.<Line>builder()
                    .baseHits(Sets.newHashSet(baseHits))
                    .noteHits(Sets.newHashSet(this.documentService.getLinesBySectionAndLineNumbers(documentId,
                            sectionSerial, lineSerials)))
                    .build();
        } else {
            return QueryHits.<Line>builder().build();
        }
    }

    public QueryHits<Section> sectionsForPerson(String documentId, String person) {
        List<String> iris = getIrisForPerson(person);
        if (!iris.isEmpty()) {
            List<Section> directHits = sectionsForPersonIri(documentId, iris);
            List<Note> noteHits = notesForIri(documentId, iris);
            return QueryHits.<Section>builder()
                    .baseHits(Sets.newHashSet(directHits))
                    .noteHits(Sets.newHashSet(this.documentService.getSectionsBySerial(documentId,
                            noteHits.stream().map(n -> (long) n.getSection()).collect(Collectors.toList()))))
                    .build();
        } else {
            return QueryHits.<Section>builder().build();
        }
    }

    public List<Section> pagesForPerson(String documentId, String person) {
        List<String> iris = getIrisForPerson(person);
        if (!iris.isEmpty()) {
            return pagesForIri(documentId, iris);
        } else {
            return Lists.newArrayList();
        }
    }
}
