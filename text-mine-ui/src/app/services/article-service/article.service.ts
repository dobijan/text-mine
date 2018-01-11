import { Injectable } from '@angular/core';
import { ArticlesApi } from '../api/generated/api/ArticlesApi';
import { Observable } from 'rxjs/Observable';
import { ArticleDTO } from '../api/generated/model/ArticleDTO';
import {ArticleDTOList} from '../api/generated/model/ArticleDTOList';

@Injectable()
export class ArticleService {

    constructor(private api: ArticlesApi) { }

    getArticle(id: string): Observable<ArticleDTO> {
        return this.api.getOne(id);
    }

    getArticles(documentId: string, sectionNumber?: number, pageNumber?: number): Observable<Array<ArticleDTO>> {
        return this.api.getByDocument(documentId, sectionNumber, pageNumber);
    }

    getFiltered(entryWord?: string, formVariant?: string, inflection?: string, pos?: Array<string>,
        documentId?: Array<string>, corpusId?: string, matchingStrategy?: string, offset?: number, limit?: number): Observable<Array<ArticleDTO>> {
        return this.api.getFiltered(entryWord, formVariant, inflection, pos, documentId, corpusId, matchingStrategy, offset, limit);
    }

    getGroupedByDocument(entryWord?: string, formVariant?: string, inflection?: string, pos?: Array<string>,
        corpusId?: string, matchingStrategy?: string, posCount?: number): Observable<{ [key: string]: ArticleDTOList; }> {
        return this.api.groupByDocument(entryWord, formVariant, inflection, pos, corpusId, matchingStrategy, posCount);
    }
}
