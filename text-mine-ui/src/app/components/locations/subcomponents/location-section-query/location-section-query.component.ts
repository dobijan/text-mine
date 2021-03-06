import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { LocationService } from './../../../../services/location-service/location.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Section } from '../../../../services/api/generated/model/Section';

@Component({
  selector: 'app-location-section-query',
  templateUrl: './location-section-query.component.html',
  styleUrls: ['./location-section-query.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class LocationSectionQueryComponent implements OnInit {

    title: String = 'Sections by Location';

    selectedId: string = null;

    location: string = null;

    documents: Array<Document> = [];

    sections: {
        baseHits: Array<Section>,
        noteHits: Array<Section>
    };

    constructor(
        private documentService: DocumentService,
        private locationService: LocationService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.sections = null;
        this.locationService.getSectionsByLocation(this.location, this.selectedId).subscribe(res => {
                this.sections = {
                    baseHits: res.baseHits,
                    noteHits: res.noteHits
                };
            },
            err => {
                console.log('Error loading Sections!');
            });
    }
}
