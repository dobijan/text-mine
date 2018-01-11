/**
 * text-mine-api
 * text-mine
 *
 * OpenAPI spec version: v1
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

/* tslint:disable:no-unused-variable member-ordering */

import { Inject, Injectable, Optional } from '@angular/core';
import { Http, Headers, URLSearchParams } from '@angular/http';
import { RequestMethod, RequestOptions, RequestOptionsArgs } from '@angular/http';
import { Response, ResponseContentType } from '@angular/http';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';

import * as models from '../model/models';
import { BASE_PATH, COLLECTION_FORMATS } from '../variables';
import { Configuration } from '../configuration';


@Injectable()
export class ArticlesApi {

    protected basePath = 'http://localhost:8080';
    public defaultHeaders: Headers = new Headers();
    public configuration: Configuration = new Configuration();

    constructor(protected http: Http, @Optional() @Inject(BASE_PATH) basePath: string, @Optional() configuration: Configuration) {
        if (basePath) {
            this.basePath = basePath;
        }
        if (configuration) {
            this.configuration = configuration;
        }
    }

    /**
     *
     * @summary Delete article
     * @param id Article Id
     */
    public deleteArticle(id: string, extraHttpRequestParams?: any): Observable<string> {
        return this.deleteArticleWithHttpInfo(id, extraHttpRequestParams)
            .map((response: Response) => {
                if (response.status === 204) {
                    return undefined;
                } else {
                    return response.json() || {};
                }
            });
    }

    /**
     *
     * @summary Get articles of a document
     * @param documentId Document Id
     * @param section Section number
     * @param page Page number
     */
    public getByDocument(documentId: string, section?: number, page?: number,
        extraHttpRequestParams?: any): Observable<Array<models.ArticleDTO>> {
        return this.getByDocumentWithHttpInfo(documentId, section, page, extraHttpRequestParams)
            .map((response: Response) => {
                if (response.status === 204) {
                    return undefined;
                } else {
                    return response.json() || {};
                }
            });
    }

    /**
     *
     * @summary Get filtered articles
     * @param entryWord Entry word
     * @param formVariant Form variant
     * @param inflection Inflection
     * @param pos Parts of speech
     * @param documentId Document ids
     * @param corpusId Corpus id
     * @param matchingStrategy Matching strategy
     * @param offset Offset
     * @param limit Limit
     */
    public getFiltered(entryWord?: string, formVariant?: string, inflection?: string, pos?: Array<string>,
        documentId?: Array<string>, corpusId?: string, matchingStrategy?: string, offset?: number, limit?: number,
        extraHttpRequestParams?: any): Observable<Array<models.ArticleDTO>> {
        return this.getFilteredWithHttpInfo(entryWord, formVariant, inflection, pos, documentId, corpusId,
            matchingStrategy, offset, limit, extraHttpRequestParams)
            .map((response: Response) => {
                if (response.status === 204) {
                    return undefined;
                } else {
                    return response.json() || {};
                }
            });
    }

    /**
     *
     * @summary Get article by Id
     * @param id Article Id
     */
    public getOne(id: string, extraHttpRequestParams?: any): Observable<models.ArticleDTO> {
        return this.getOneWithHttpInfo(id, extraHttpRequestParams)
            .map((response: Response) => {
                if (response.status === 204) {
                    return undefined;
                } else {
                    return response.json() || {};
                }
            });
    }

    /**
     *
     * @summary Get articles grouped by document id
     * @param entryWord Entry word
     * @param formVariant Form variant
     * @param inflection Inflection
     * @param pos Parts of speech
     * @param corpusId Corpus Id
     * @param matchingStrategy Matching strategy
     * @param posCount Part of speech count
     */
    public groupByDocument(entryWord?: string, formVariant?: string, inflection?: string, pos?: Array<string>,
        corpusId?: string, matchingStrategy?: string, posCount?: number, extraHttpRequestParams?: any):
        Observable<{ [key: string]: models.ArticleDTOList; }> {
        return this.groupByDocumentWithHttpInfo(entryWord, formVariant, inflection, pos, corpusId, matchingStrategy,
            posCount, extraHttpRequestParams)
            .map((response: Response) => {
                if (response.status === 204) {
                    return undefined;
                } else {
                    return response.json() || {};
                }
            });
    }

    /**
     *
     * @summary Update article
     * @param body Article
     * @param id Article id
     */
    public putArticle(body: models.Article, id: string, extraHttpRequestParams?: any): Observable<Array<models.ArticleDTO>> {
        return this.putArticleWithHttpInfo(body, id, extraHttpRequestParams)
            .map((response: Response) => {
                if (response.status === 204) {
                    return undefined;
                } else {
                    return response.json() || {};
                }
            });
    }


    /**
     * Delete article
     *
     * @param id Article Id
     */
    public deleteArticleWithHttpInfo(id: string, extraHttpRequestParams?: any): Observable<Response> {
        const path = this.basePath + '/articles/${id}'
            .replace('${' + 'id' + '}', String(id));

        const queryParameters = new URLSearchParams();
        const headers = new Headers(this.defaultHeaders.toJSON()); // https://github.com/angular/angular/issues/6845
        // verify required parameter 'id' is not null or undefined
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling deleteArticle.');
        }
        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        // to determine the Accept header
        const produces: string[] = [
            'application/json;charset=UTF-8'
        ];

        let requestOptions: RequestOptionsArgs = new RequestOptions({
            method: RequestMethod.Delete,
            headers: headers,
            search: queryParameters,
            withCredentials: this.configuration.withCredentials
        });
        // https://github.com/swagger-api/swagger-codegen/issues/4037
        if (extraHttpRequestParams) {
            requestOptions = (<any>Object).assign(requestOptions, extraHttpRequestParams);
        }

        return this.http.request(path, requestOptions);
    }

    /**
     * Get articles of a document
     *
     * @param documentId Document Id
     * @param section Section number
     * @param page Page number
     */
    public getByDocumentWithHttpInfo(documentId: string, section?: number, page?: number,
        extraHttpRequestParams?: any): Observable<Response> {
        const path = this.basePath + '/articles/by-document/${documentId}'
            .replace('${' + 'documentId' + '}', String(documentId));

        const queryParameters = new URLSearchParams();
        const headers = new Headers(this.defaultHeaders.toJSON()); // https://github.com/angular/angular/issues/6845
        // verify required parameter 'documentId' is not null or undefined
        if (documentId === null || documentId === undefined) {
            throw new Error('Required parameter documentId was null or undefined when calling getByDocument.');
        }
        if (section !== undefined) {
            queryParameters.set('section', <any>section);
        }

        if (page !== undefined) {
            queryParameters.set('page', <any>page);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        // to determine the Accept header
        const produces: string[] = [
            'application/json;charset=UTF-8'
        ];

        let requestOptions: RequestOptionsArgs = new RequestOptions({
            method: RequestMethod.Get,
            headers: headers,
            search: queryParameters,
            withCredentials: this.configuration.withCredentials
        });
        // https://github.com/swagger-api/swagger-codegen/issues/4037
        if (extraHttpRequestParams) {
            requestOptions = (<any>Object).assign(requestOptions, extraHttpRequestParams);
        }

        return this.http.request(path, requestOptions);
    }

    /**
     * Get filtered articles
     *
     * @param entryWord Entry word
     * @param formVariant Form variant
     * @param inflection Inflection
     * @param pos Parts of speech
     * @param documentId Document ids
     * @param corpusId Corpus id
     * @param matchingStrategy Matching strategy
     * @param offset Offset
     * @param limit Limit
     */
    public getFilteredWithHttpInfo(entryWord?: string, formVariant?: string, inflection?: string, pos?: Array<string>,
        documentId?: Array<string>, corpusId?: string, matchingStrategy?: string, offset?: number, limit?: number,
        extraHttpRequestParams?: any): Observable<Response> {
        const path = this.basePath + '/articles';

        const queryParameters = new URLSearchParams();
        const headers = new Headers(this.defaultHeaders.toJSON()); // https://github.com/angular/angular/issues/6845
        if (entryWord !== undefined) {
            queryParameters.set('entryWord', <any>entryWord);
        }

        if (formVariant !== undefined) {
            queryParameters.set('formVariant', <any>formVariant);
        }

        if (inflection !== undefined) {
            queryParameters.set('inflection', <any>inflection);
        }

        if (pos) {
            pos.forEach((element) => {
                queryParameters.append('pos', <any>element);
            });
        }

        if (documentId) {
            documentId.forEach((element) => {
                queryParameters.append('documentId', <any>element);
            });
        }

        if (corpusId !== undefined) {
            queryParameters.set('corpusId', <any>corpusId);
        }

        if (matchingStrategy !== undefined) {
            queryParameters.set('matchingStrategy', <any>matchingStrategy);
        }

        if (offset !== undefined) {
            queryParameters.set('offset', <any>offset);
        }

        if (limit !== undefined) {
            queryParameters.set('limit', <any>limit);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        // to determine the Accept header
        const produces: string[] = [
            'application/json;charset=UTF-8'
        ];

        let requestOptions: RequestOptionsArgs = new RequestOptions({
            method: RequestMethod.Get,
            headers: headers,
            search: queryParameters,
            withCredentials: this.configuration.withCredentials
        });
        // https://github.com/swagger-api/swagger-codegen/issues/4037
        if (extraHttpRequestParams) {
            requestOptions = (<any>Object).assign(requestOptions, extraHttpRequestParams);
        }

        return this.http.request(path, requestOptions);
    }

    /**
     * Get article by Id
     *
     * @param id Article Id
     */
    public getOneWithHttpInfo(id: string, extraHttpRequestParams?: any): Observable<Response> {
        const path = this.basePath + '/articles/${id}'
            .replace('${' + 'id' + '}', String(id));

        const queryParameters = new URLSearchParams();
        const headers = new Headers(this.defaultHeaders.toJSON()); // https://github.com/angular/angular/issues/6845
        // verify required parameter 'id' is not null or undefined
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling getOne.');
        }
        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        // to determine the Accept header
        const produces: string[] = [
            'application/json;charset=UTF-8'
        ];

        let requestOptions: RequestOptionsArgs = new RequestOptions({
            method: RequestMethod.Get,
            headers: headers,
            search: queryParameters,
            withCredentials: this.configuration.withCredentials
        });
        // https://github.com/swagger-api/swagger-codegen/issues/4037
        if (extraHttpRequestParams) {
            requestOptions = (<any>Object).assign(requestOptions, extraHttpRequestParams);
        }

        return this.http.request(path, requestOptions);
    }

    /**
     * Get articles grouped by document id
     *
     * @param entryWord Entry word
     * @param formVariant Form variant
     * @param inflection Inflection
     * @param pos Parts of speech
     * @param corpusId Corpus Id
     * @param matchingStrategy Matching strategy
     * @param posCount Part of speech count
     */
    public groupByDocumentWithHttpInfo(entryWord?: string, formVariant?: string, inflection?: string,
        pos?: Array<string>, corpusId?: string, matchingStrategy?: string, posCount?: number,
        extraHttpRequestParams?: any): Observable<Response> {
        const path = this.basePath + '/articles/group/by-document';

        const queryParameters = new URLSearchParams();
        const headers = new Headers(this.defaultHeaders.toJSON()); // https://github.com/angular/angular/issues/6845
        if (entryWord !== undefined) {
            queryParameters.set('entryWord', <any>entryWord);
        }

        if (formVariant !== undefined) {
            queryParameters.set('formVariant', <any>formVariant);
        }

        if (inflection !== undefined) {
            queryParameters.set('inflection', <any>inflection);
        }

        if (pos) {
            pos.forEach((element) => {
                queryParameters.append('pos', <any>element);
            });
        }

        if (corpusId !== undefined) {
            queryParameters.set('corpusId', <any>corpusId);
        }

        if (matchingStrategy !== undefined) {
            queryParameters.set('matchingStrategy', <any>matchingStrategy);
        }

        if (posCount !== undefined) {
            queryParameters.set('posCount', <any>posCount);
        }

        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        // to determine the Accept header
        const produces: string[] = [
            'application/json;charset=UTF-8'
        ];

        let requestOptions: RequestOptionsArgs = new RequestOptions({
            method: RequestMethod.Get,
            headers: headers,
            search: queryParameters,
            withCredentials: this.configuration.withCredentials
        });
        // https://github.com/swagger-api/swagger-codegen/issues/4037
        if (extraHttpRequestParams) {
            requestOptions = (<any>Object).assign(requestOptions, extraHttpRequestParams);
        }

        return this.http.request(path, requestOptions);
    }

    /**
     * Update article
     *
     * @param body Article
     * @param id Article id
     */
    public putArticleWithHttpInfo(body: models.Article, id: string, extraHttpRequestParams?: any): Observable<Response> {
        const path = this.basePath + '/articles/${id}'
            .replace('${' + 'id' + '}', String(id));

        const queryParameters = new URLSearchParams();
        const headers = new Headers(this.defaultHeaders.toJSON()); // https://github.com/angular/angular/issues/6845
        // verify required parameter 'body' is not null or undefined
        if (body === null || body === undefined) {
            throw new Error('Required parameter body was null or undefined when calling putArticle.');
        }
        // verify required parameter 'id' is not null or undefined
        if (id === null || id === undefined) {
            throw new Error('Required parameter id was null or undefined when calling putArticle.');
        }
        // to determine the Content-Type header
        const consumes: string[] = [
        ];

        // to determine the Accept header
        const produces: string[] = [
            'application/json;charset=UTF-8'
        ];

        headers.set('Content-Type', 'application/json');

        let requestOptions: RequestOptionsArgs = new RequestOptions({
            method: RequestMethod.Put,
            headers: headers,
            body: body == null ? '' : JSON.stringify(body), // https://github.com/angular/angular/issues/10612
            search: queryParameters,
            withCredentials: this.configuration.withCredentials
        });
        // https://github.com/swagger-api/swagger-codegen/issues/4037
        if (extraHttpRequestParams) {
            requestOptions = (<any>Object).assign(requestOptions, extraHttpRequestParams);
        }

        return this.http.request(path, requestOptions);
    }

}
