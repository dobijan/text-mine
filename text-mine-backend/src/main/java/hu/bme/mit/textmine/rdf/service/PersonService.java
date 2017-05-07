package hu.bme.mit.textmine.rdf.service;

import org.springframework.stereotype.Service;

@Service
public class PersonService extends RdfResourceService {

    @Override
    protected void setRelationType() {
        this.relationType = this.vocabulary.personRelation();
    }
}
