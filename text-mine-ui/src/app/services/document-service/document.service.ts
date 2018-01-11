import { Injectable } from '@angular/core';
import { DocumentsApi } from './../api/generated/api/DocumentsApi';
import { Observable } from 'rxjs/Observable';
import { Document } from './../api/generated/model/Document';
import { SectionHits } from './../api/generated/model/SectionHits';
import { LineHits } from './../api/generated/model/LineHits';
import { PartOfSpeechStatistics } from '../api/generated/model/PartOfSpeechStatistics';
import { PosShingleEntry } from '../api/generated/model/PosShingleEntry';


@Injectable()
export class DocumentService {

    constructor(private api: DocumentsApi) { }

    getDocuments(): Observable<Array<Document>> {
        return this.api.getFiltered();
    }

    getDocument(id: string): Observable<Document> {
        return this.api.getOne(id);
    }

    getDocumentsByCorpus(id: string): Observable<Array<Document>> {
        return this.api.getByCorpus(id);
    }

    getFilteredDocuments(entryText?: string, pos?: Array<string>, entryWord?: string, documentId?: Array<string>,
        offset?: number, limit?: number): Observable<Array<Document>> {
        return this.api.getFiltered(entryText, pos, !entryWord ? [] : [entryWord], documentId, offset, limit);
    }

    getFilteredSections(id: string, phrase?: string, pos?: Array<string>, slop?: number, disjoint?: boolean):
        Observable<SectionHits> {
        return this.api.getFilteredSections(id, [phrase], pos, slop, disjoint);
    }

    getFilteredPages(id: string, phrase?: string, pos?: Array<string>, slop?: number, disjoint?: boolean):
        Observable<SectionHits> {
        return this.api.getFilteredPages(id, [phrase], pos, slop, disjoint);
    }

    getFilteredLines(id: string, serial: number, phrase?: string):
        Observable<LineHits> {
        return this.api.getFilteredLines(id, serial, [phrase]);
    }

    getPosStats(id: string): Observable<Array<PartOfSpeechStatistics>> {
        return this.api.getPosStats(id);
    }

    getShingleStats(limit: number): Observable<Array<PosShingleEntry>> {
        return this.api.getShingleStats(limit);
    }

    getPosShingleStats(pos: Array<string>, limit: number): Observable<Array<PosShingleEntry>> {
        return this.api.getPosShingleStats(pos, limit);
    }

    getSectionShingleStats(id: string, limit: number): Observable<Array<PosShingleEntry>> {
        return this.api.getSectionShingleStats(id, limit);
    }

    getSectionPosShingleStats(id: string, pos: Array<string>, limit: number): Observable<Array<PosShingleEntry>> {
        return this.api.getSectionPosShingleStats(id, pos, limit);
    }

    getPageShingleStats(id: string, limit: number): Observable<Array<PosShingleEntry>> {
        return this.api.getPageShingleStats(id, limit);
    }

    getPagePosShingleStats(id: string, pos: Array<string>, limit: number): Observable<Array<PosShingleEntry>> {
        return this.api.getPagePosShingleStats(id, pos, limit);
    }
}
