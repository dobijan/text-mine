import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { LocationService } from './../../../../services/location-service/location.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Line } from '../../../../services/api/generated/model/Line';

@Component({
  selector: 'app-location-line-query',
  templateUrl: './location-line-query.component.html',
  styleUrls: ['./location-line-query.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class LocationLineQueryComponent implements OnInit {

    title: String = 'Lines by Location';

    selectedId: string = null;

    serial: number = null;

    location: string = null;

    documents: Array<Document> = [];

    lines: {
        baseHits: Array<Line>,
        noteHits: Array<Line>
    };

    constructor(
        private documentService: DocumentService,
        private locationService: LocationService) { }

    ngOnInit() {
        this.documentService.getDocuments().subscribe(res => this.documents = res);
    }

    onSubmit() {
        this.lines = null;
        this.locationService.getLinesByLocation(this.location, this.selectedId, this.serial).subscribe(res => {
            this.lines = {
                baseHits: res.baseHits,
                noteHits: res.noteHits
            };
        },
            err => {
                console.log('Error loading Lines!');
            });
    }

}
