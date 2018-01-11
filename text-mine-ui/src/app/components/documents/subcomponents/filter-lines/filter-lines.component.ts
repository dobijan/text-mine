import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Line } from '../../../../services/api/generated/model/Line';
import { MatSnackBar } from '@angular/material';

@Component({
    selector: 'app-filter-lines',
    templateUrl: './filter-lines.component.html',
    styleUrls: ['./filter-lines.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class FilterLinesComponent implements OnInit {

    title: String = 'Filter Lines';

    selectedId: string = null;

    documents: Array<Document> = [];

    progressMode: String = 'determinate';

    progressValue = 0;

    lines: {
        baseHits: Array<Line>,
        noteHits: Array<Line>
    };

    filters = {
        phrase: null,
        serial: null
    };

    constructor(
        private documentService: DocumentService,
        private snackBar: MatSnackBar) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        if (this.filters.serial == null || this.filters.phrase == null || this.selectedId == null) {
            this.snackBar.open('Please provide the document ID, section serial and phrase!', 'Sorry! I will.', {
                duration: 10000,
            });
        } else {
            this.progressMode = 'indeterminate';
            this.progressValue = 0;
            this.lines = null;
            this.documentService.getFilteredLines(this.selectedId, this.filters.serial, this.filters.phrase).subscribe(res => {
                this.lines = {
                    baseHits: res.baseHits,
                    noteHits: res.noteHits
                };
                this.progressMode = 'determinate';
                this.progressValue = 100;
            },
                err => {
                    console.log('Error loading Sections!');
                    this.progressMode = 'determinate';
                    this.progressValue = 0;
                });
        }
    }
}
