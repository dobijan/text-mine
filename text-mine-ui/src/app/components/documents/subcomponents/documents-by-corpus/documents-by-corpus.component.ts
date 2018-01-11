import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Corpus } from '../../../../services/api/generated/model/Corpus';
import { CorpusService } from './../../../../services/corpus-service/corpus.service';


@Component({
    selector: 'app-documents-by-corpus',
    templateUrl: './documents-by-corpus.component.html',
    styleUrls: ['./documents-by-corpus.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class DocumentsByCorpusComponent implements OnInit {

    title: String = 'Documents by Corpus';

    selectedId: string = null;

    documents: Array<Document> = null;

    corpora: Array<Corpus> = [];

    progressMode: String = 'determinate';

    progressValue = 0;

    constructor(
        private documentService: DocumentService,
        private corpusService: CorpusService) { }

    ngOnInit() {
        this.corpusService.getCorpora().subscribe(res => this.corpora = res);
    }

    onSubmit() {
        this.documents = null;
        this.progressMode = 'indeterminate';
        this.progressValue = 0;
        this.documentService.getDocumentsByCorpus(this.selectedId)
            .subscribe(res => {
                this.documents = res;
                this.progressMode = 'determinate';
                this.progressValue = 100;
            },
            err => {
                console.log('Error loading Documents!');
                this.progressMode = 'determinate';
                this.progressValue = 0;
            });
    }

}
