import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { ArticleService } from '../../../../services/article-service/article.service';
import { ArticleDTO } from '../../../../services/api/generated/model/ArticleDTO';
import { DocumentService } from './../../../../services/document-service/document.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { MatSnackBar } from '@angular/material';

@Component({
    selector: 'app-articles-by-document',
    templateUrl: './articles-by-document.component.html',
    styleUrls: ['./articles-by-document.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class ArticlesByDocumentComponent implements OnInit {

    title: String = 'Article by Document';

    documents: Array<Document> = [];

    selectedId: string = null;

    sectionNumber = null;

    pageNumber = null;

    progressMode: String = 'determinate';

    progressValue = 0;

    articles: Array<ArticleDTO> = [];

    constructor(
        private articleService: ArticleService,
        private documentService: DocumentService,
        private snackBar: MatSnackBar
    ) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        if (this.sectionNumber == null && this.pageNumber == null) {
            this.snackBar.open('Please provide a page or a section number!', 'Sorry! I will.', {
                duration: 10000,
            });
        } else {
            this.articles = null;
            this.progressMode = 'indeterminate';
            this.progressValue = 0;
            this.articleService.getArticles(this.selectedId, this.sectionNumber, this.pageNumber)
                .subscribe(res => {
                    this.articles = res;
                    this.progressMode = 'determinate';
                    this.progressValue = 100;
                },
                err => {
                    console.log('Error loading Articles!');
                    this.progressMode = 'determinate';
                    this.progressValue = 0;
                });
        }
    }

}
