import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { LocationService } from './../../../../services/location-service/location.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Section } from '../../../../services/api/generated/model/Section';

@Component({
  selector: 'app-location-page-query',
  templateUrl: './location-page-query.component.html',
  styleUrls: ['./location-page-query.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class LocationPageQueryComponent implements OnInit {

    title: String = 'Pages by Location';

    selectedId: string = null;

    location: string = null;

    documents: Array<Document> = [];

    pages: Array<Section> = null;

    constructor(
        private documentService: DocumentService,
        private locationService: LocationService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.pages = null;
        this.locationService.getPagesByLocation(this.location, this.selectedId).subscribe(res => {
                this.pages = res;
            },
            err => {
                console.log('Error loading Pages!');
            });
    }
}
