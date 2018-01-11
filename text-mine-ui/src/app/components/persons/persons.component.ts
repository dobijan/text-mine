import { Component, OnInit, ViewEncapsulation } from '@angular/core';

@Component({
    selector: 'app-persons',
    templateUrl: './persons.component.html',
    styleUrls: ['./persons.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class PersonsComponent implements OnInit {

    title = 'Persons';

    constructor() { }

    ngOnInit() {
    }

}
