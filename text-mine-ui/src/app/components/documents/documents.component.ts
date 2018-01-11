import { Component, OnInit, ViewEncapsulation } from '@angular/core';

@Component({
    selector: 'app-documents',
    templateUrl: './documents.component.html',
    styleUrls: ['./documents.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class DocumentsComponent implements OnInit {

    title: String = 'Documents';

    constructor() { }

    ngOnInit() {
    }

}
