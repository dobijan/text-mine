import { Component, OnInit, ViewEncapsulation } from '@angular/core';

@Component({
    selector: 'app-stats',
    templateUrl: './stats.component.html',
    styleUrls: ['./stats.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class StatsComponent implements OnInit {

    title = 'Statistics';

    constructor() { }

    ngOnInit() {
    }

}
