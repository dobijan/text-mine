import { Component, OnInit, ViewEncapsulation } from '@angular/core';

@Component({
    selector: 'app-articles',
    templateUrl: './articles.component.html',
    styleUrls: ['./articles.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class ArticlesComponent implements OnInit {

    title: String = 'Articles';

    constructor() { }

    ngOnInit() {
    }

}
