import { Injectable } from '@angular/core';
import { PersonsApi } from '../api/generated/api/PersonsApi';
import { Observable } from 'rxjs/Observable';
import { SectionHits } from '../api/generated/model/SectionHits';
import { LineHits } from '../api/generated/model/LineHits';
import { Section } from '../api/generated/model/Section';

@Injectable()
export class PersonService {

    constructor(private api: PersonsApi) { }

    exists(name: string): Observable<boolean> {
        return this.api.exists(name);
    }

    getSectionsByPerson(person: string, documentId: string): Observable<SectionHits> {
        return this.api.sectionQuery(person, documentId);
    }

    getPagesByPerson(person: string, documentId: string): Observable<Array<Section>> {
        return this.api.pagesQuery(documentId, person);
    }

    getLinesByPerson(person: string, documentId: string, serial: number): Observable<LineHits> {
        return this.api.lineQuery(documentId, person, serial);
    }
}
