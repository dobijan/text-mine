import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { Document } from '../../../../services/api/generated/model/Document';

@Component({
    selector: 'app-documents-by-id',
    templateUrl: './documents-by-id.component.html',
    styleUrls: ['./documents-by-id.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class DocumentsByIdComponent implements OnInit {

    title: String = 'Document by ID';

    documentId: string = null;

    document: Document = null;

    constructor(private documentService: DocumentService) { }

    ngOnInit() {
    }

    onSubmit() {
        this.document = null;
        this.documentService.getDocument(this.documentId).subscribe(res => this.document = res);
    }

}
