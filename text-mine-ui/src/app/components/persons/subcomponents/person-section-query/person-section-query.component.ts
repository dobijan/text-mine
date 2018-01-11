import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { PersonService } from './../../../../services/person-service/person.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Section } from '../../../../services/api/generated/model/Section';

@Component({
    selector: 'app-person-section-query',
    templateUrl: './person-section-query.component.html',
    styleUrls: ['./person-section-query.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class PersonSectionQueryComponent implements OnInit {

    title: String = 'Sections by Person';

    selectedId: string = null;

    person: string = null;

    documents: Array<Document> = [];

    sections: {
        baseHits: Array<Section>,
        noteHits: Array<Section>
    };

    constructor(
        private documentService: DocumentService,
        private personService: PersonService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.sections = null;
        this.personService.getSectionsByPerson(this.person, this.selectedId).subscribe(res => {
            this.sections = {
                baseHits: res.baseHits,
                noteHits: res.noteHits
            };
        },
            err => {
                console.log('Error loading Sections!');
            });
    }

}
