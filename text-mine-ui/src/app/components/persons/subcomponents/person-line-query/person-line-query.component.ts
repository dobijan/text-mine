import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { PersonService } from './../../../../services/person-service/person.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Line } from '../../../../services/api/generated/model/Line';

@Component({
  selector: 'app-person-line-query',
  templateUrl: './person-line-query.component.html',
  styleUrls: ['./person-line-query.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class PersonLineQueryComponent implements OnInit {

    title: String = 'Lines by Person';

    selectedId: string = null;

    serial: number = null;

    person: string = null;

    documents: Array<Document> = [];

    lines: {
        baseHits: Array<Line>,
        noteHits: Array<Line>
    };

    constructor(
        private documentService: DocumentService,
        private personService: PersonService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.lines = null;
        this.personService.getLinesByPerson(this.selectedId, this.person, this.serial).subscribe(res => {
            this.lines = {
                baseHits: res.baseHits,
                noteHits: res.noteHits
            };
        },
            err => {
                console.log('Error loading Lines!');
            });
    }

}
