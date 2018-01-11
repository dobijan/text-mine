import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { PersonService } from './../../../../services/person-service/person.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Section } from '../../../../services/api/generated/model/Section';

@Component({
    selector: 'app-person-page-query',
    templateUrl: './person-page-query.component.html',
    styleUrls: ['./person-page-query.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class PersonPageQueryComponent implements OnInit {

    title: String = 'Pages by Person';

    selectedId: string = null;

    person: string = null;

    documents: Array<Document> = [];

    pages: Array<Section> = null;

    constructor(
        private documentService: DocumentService,
        private personService: PersonService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.pages = null;
        this.personService.getPagesByPerson(this.person, this.selectedId).subscribe(res => {
            this.pages = res;
        },
            err => {
                console.log('Error loading Pages!');
            });
    }

}
