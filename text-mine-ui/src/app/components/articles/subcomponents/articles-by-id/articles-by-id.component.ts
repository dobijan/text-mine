import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import {ArticleService} from '../../../../services/article-service/article.service';
import {ArticleDTO} from '../../../../services/api/generated/model/ArticleDTO';

@Component({
  selector: 'app-articles-by-id',
  templateUrl: './articles-by-id.component.html',
  styleUrls: ['./articles-by-id.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ArticlesByIdComponent implements OnInit {

    title: String = 'Article by ID';

    articleId: string = null;

    article: ArticleDTO = null;

    constructor(private articleService: ArticleService) { }

    ngOnInit() {
    }

    onSubmit() {
        this.article = null;
        this.articleService.getArticle(this.articleId).subscribe(res => this.article = res);
    }

}
