import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Note } from '../../../../services/api/generated/model/Note';
import { NoteService } from '../../../../services/note-service/note.service';

@Component({
    selector: 'app-all-notes',
    templateUrl: './all-notes.component.html',
    styleUrls: ['./all-notes.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class AllNotesComponent implements OnInit {

    title: String = 'All Notes';

    progressMode: String = 'determinate';

    progressValue = 0;

    notes: Array<Note> = [];

    constructor(private service: NoteService) { }

    ngOnInit() {
    }

    onSubmit() {
        this.progressMode = 'indeterminate';
        this.progressValue = 0;
        this.service.getNotes().subscribe(res => {
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
