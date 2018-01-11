import { Injectable } from '@angular/core';
import { NotesApi } from './../api/generated/api/NotesApi';
import { Observable } from 'rxjs/Observable';
import { Note } from './../api/generated/model/Note';

@Injectable()
export class NoteService {

    constructor(private api: NotesApi) { }

    getNotes(): Observable<Array<Note>> {
        return this.api.getAll();
    }

    getNotesByDocumentId(documentId: string): Observable<Array<Note>> {
        return this.api.getByDocument(documentId);
    }

    getNote(id: string): Observable<Note> {
        return this.api.getOne(id);
    }

    // getCollections(): Observable<CollectionsResponse> {
    //     return this.api.getCollections();
    // }
    //
    // index(content: string, collectionId: string, stemming: boolean, unhyphenation: boolean): Observable<Document> {
    //     const resp = this.api.index({
    //         collectionId: collectionId,
    //         content: content,
    //         stemming: stemming,
    //         unhyphenation: unhyphenation
    //     }).map(res => {
    //         const doc = <Document>res.content;
    //         return doc;
    //     });
    //     return resp;
    // }
    //
    // search(phrase: string, collectionId: string, stemming: boolean, pagination: Pagination): Observable<SearchResponse> {
    //     return this.api.search({
    //         stemming: stemming,
    //         collectionId: collectionId,
    //         pagination: pagination,
    //         phrase: phrase,
    //     });
    // }
    //
    // count(phrase: string, collectionId: string, stemming: boolean): Observable<CountResponse> {
    //     return this.api.count({
    //         stemming: stemming,
    //         collectionId: collectionId,
    //         phrase: phrase,
    //     });
    // }
    //
    // stem(text: string, stemming: boolean, searching: boolean, unhyphenation: boolean): Observable<StemmingResponse> {
    //     return this.api.stem({
    //         stemming: stemming,
    //         searching: searching,
    //         text: text,
    //         unhyphenation: unhyphenation
    //     });
    // }
    //
    // getDocument(collectionId: string, documentId: string): Observable<Document> {
    //     return this.api.getDocument({
    //         collectionId: collectionId,
    //         documentId: documentId
    //     }).map(res => {
    //         const doc = <Document>res.content;
    //         return doc;
    //     });
    // }
    //
    // detectLanguage(text: string, option: LanguageDetectionRequest.OptionEnum): Observable<LanguageDetectionResponse> {
    //     return this.api.detectLanguage({
    //         option: option,
    //         content: text
    //     });
    // }
    //
    // detectCharset(text: string): Observable<CharsetResponse> {
    //     const bytes = Uint8Array.from(text.split('').map(s => s.charCodeAt(0)));
    //     const headers = new Headers();
    //     headers.set('Content-Type', 'application/octet-stream');
    //     const requestOptions: RequestOptionsArgs = new RequestOptions({
    //         method: RequestMethod.Post,
    //         headers: headers,
    //         body: bytes.buffer
    //     });
    //
    //     return this.http.request('http://localhost:8080/search/encoding', requestOptions).map((response: Response) => {
    //         if (response.status === 204) {
    //             console.log(response);
    //             return undefined;
    //         } else {
    //             return response.json();
    //         }
    //     });
    // }
    //
    // detecCharsetFromUri(uri: string): Observable<CharsetResponse> {
    //     return this.api.detectEncoding({
    //         uri: uri
    //     });
    // }
    //
    // cleanMarkup(text: string): Observable<CleanMarkupResponse> {
    //     return this.api.cleanMarkup({
    //         text: text
    //     });
    // }
    //
    // extractLinks(text: string): Observable<string[]> {
    //     return this.api.getLinks({
    //         content: text
    //     });
    // }
}
