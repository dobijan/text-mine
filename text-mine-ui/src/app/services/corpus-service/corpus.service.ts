import { Injectable } from '@angular/core';
import { CorporaApi } from './../api/generated/api/CorporaApi';
import { Observable } from 'rxjs/Observable';
import { Corpus } from './../api/generated/model/Corpus';

@Injectable()
export class CorpusService {

    constructor(private api: CorporaApi) { }

    getCorpora(): Observable<Array<Corpus>> {
        return this.api.getAll();
    }
}
