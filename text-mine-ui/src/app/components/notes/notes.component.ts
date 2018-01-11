import { Component, OnInit, ViewEncapsulation } from '@angular/core';

@Component({
    selector: 'app-notes',
    templateUrl: './notes.component.html',
    styleUrls: ['./notes.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class NotesComponent implements OnInit {

    title: String = 'Notes';

    constructor() { }

    ngOnInit() {
    }

}
