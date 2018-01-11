import { Injectable } from '@angular/core';
import { LocationsApi } from '../api/generated/api/LocationsApi';
import { Observable } from 'rxjs/Observable';
import { SectionHits } from '../api/generated/model/SectionHits';
import { LineHits } from '../api/generated/model/LineHits';
import { Section } from '../api/generated/model/Section';
import { SpatialQueryResult } from '../api/generated/model/SpatialQueryResult';

@Injectable()
export class LocationService {

    constructor(private api: LocationsApi) { }

    exists(name: string): Observable<boolean> {
        return this.api.exists(name);
    }

    getSectionsByLocation(location: string, documentId: string): Observable<SectionHits> {
        return this.api.sectionQuery(location, documentId);
    }

    getPagesByLocation(location: string, documentId: string): Observable<Array<Section>> {
        return this.api.pagesQuery(documentId, location);
    }

    getLinesByLocation(location: string, documentId: string, serial: number): Observable<LineHits> {
        return this.api.lineQuery(documentId, serial, location);
    }

    spatialQuery(iri: string, radius: number): Observable<Array<SpatialQueryResult>> {
        return this.api.spatialQuery({
            iri: iri,
            radius: radius
        });
    }

    sectionSpatialQuery(iri: string, radius: number): Observable<Array<SpatialQueryResult>> {
        return this.api.sectionSpatialQuery({
            iri: iri,
            radius: radius
        });
    }
}
