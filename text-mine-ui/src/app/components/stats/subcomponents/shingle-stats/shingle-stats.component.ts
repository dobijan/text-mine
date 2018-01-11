import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { PosShingleEntry } from '../../../../services/api/generated/model/PosShingleEntry';

@Component({
    selector: 'app-shingle-stats',
    templateUrl: './shingle-stats.component.html',
    styleUrls: ['./shingle-stats.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class ShingleStatsComponent implements OnInit {

    title: String = 'Shingle Stats';

    limit: number = null;

    stats: Array<PosShingleEntry> = [];

    constructor(private documentService: DocumentService) { }

    ngOnInit() {
    }

    onSubmit() {
        this.stats = null;
        this.documentService.getShingleStats(this.limit).subscribe(res => {
            this.stats = res.map(obj => {
                return {
                    key: Object.getOwnPropertyNames(obj)[0],
                    value: obj[Object.getOwnPropertyNames(obj)[0]]
                };
            });
        },
            err => {
                console.log('Error loading Part of Speech Stats!');
            });
    }
}
