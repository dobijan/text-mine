import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { Document } from '../../../../services/api/generated/model/Document';

@Component({
    selector: 'app-pos-stats',
    templateUrl: './pos-stats.component.html',
    styleUrls: ['./pos-stats.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class PosStatsComponent implements OnInit {

    title: String = 'Part of Speech Stats';

    selectedId: string = null;

    documents: Array<Document> = [];

    stats: {
        counts: Array<{
            pos: string,
            count: number
        }>,
        proportions: Array<{
            pos: string,
            proportion: number
        }>
    };

    constructor(private documentService: DocumentService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.stats = null;
        this.documentService.getPosStats(this.selectedId).subscribe(res => {
            this.stats = {
                counts: Object.entries(res[0].count).map(entry => {
                    return {
                        pos: entry[0],
                        count: entry[1]
                    };
                }),
                proportions: Object.entries(res[0].proportions).map(entry => {
                    return {
                        pos: entry[0],
                        proportion: entry[1]
                    };
                })
            };
        },
            err => {
                console.log('Error loading Part of Speech Stats!');
            });
    }

}
