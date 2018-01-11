import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { NoteService } from './../../../../services/note-service/note.service';
import { Note } from '../../../../services/api/generated/model/Note';
import { Document } from '../../../../services/api/generated/model/Document';

@Component({
    selector: 'app-notes-by-document',
    templateUrl: './notes-by-document.component.html',
    styleUrls: ['./notes-by-document.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class NotesByDocumentComponent implements OnInit {

    documents: Array<Document> = [];

    title: String = 'Notes by Document ID';

    selectedId: string = null;

    progressMode: String = 'determinate';

    progressValue = 0;

    notes: Array<Note> = [];

    constructor(
        private documentService: DocumentService,
        private noteService: NoteService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.progressMode = 'indeterminate';
        this.progressValue = 0;
        this.noteService.getNotesByDocumentId(this.selectedId).subscribe(res => {
            this.notes = res;
            this.progressMode = 'determinate';
            this.progressValue = 100;
        },
            err => {
                console.log('Error loading Notes!');
                this.progressMode = 'determinate';
                this.progressValue = 0;
            });
    }
}
