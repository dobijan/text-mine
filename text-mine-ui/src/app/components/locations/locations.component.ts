import { Component, OnInit, ViewEncapsulation } from '@angular/core';

@Component({
    selector: 'app-locations',
    templateUrl: './locations.component.html',
    styleUrls: ['./locations.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class LocationsComponent implements OnInit {

    title = 'Locations';

    constructor() { }

    ngOnInit() {
    }

}
