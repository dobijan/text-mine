import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Note } from '../../../../services/api/generated/model/Note';
import { NoteService } from './../../../../services/note-service/note.service';


@Component({
    selector: 'app-notes-by-id',
    templateUrl: './notes-by-id.component.html',
    styleUrls: ['./notes-by-id.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class NotesByIdComponent implements OnInit {

    title: String = 'Note by ID';

    noteId: string = null;

    note: Note = null;

    constructor(private noteService: NoteService) { }

    ngOnInit() {
    }

    onSubmit() {
        this.note = null;
        this.noteService.getNote(this.noteId).subscribe(res => this.note = res);
    }
}
