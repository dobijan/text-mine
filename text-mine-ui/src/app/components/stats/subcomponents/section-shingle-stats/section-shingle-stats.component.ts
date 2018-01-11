import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { PosShingleEntry } from '../../../../services/api/generated/model/PosShingleEntry';

@Component({
  selector: 'app-section-shingle-stats',
  templateUrl: './section-shingle-stats.component.html',
  styleUrls: ['./section-shingle-stats.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class SectionShingleStatsComponent implements OnInit {

    title: String = 'Section Shingle Stats';

    limit: number = null;

    documentId: string = null;

    documents: Array<Document> = [];

    stats: Array<PosShingleEntry> = [];

    constructor(private documentService: DocumentService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.stats = null;
        this.documentService.getSectionShingleStats(this.documentId, this.limit).subscribe(res => {
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
