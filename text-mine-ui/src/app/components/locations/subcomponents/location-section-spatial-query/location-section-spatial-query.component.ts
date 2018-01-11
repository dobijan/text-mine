import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { DocumentService } from './../../../../services/document-service/document.service';
import { LocationService } from './../../../../services/location-service/location.service';
import { Document } from '../../../../services/api/generated/model/Document';
import { Section } from '../../../../services/api/generated/model/Section';
import { SpatialQueryResult } from '../../../../services/api/generated/model/SpatialQueryResult';
import { SpatialQueryRequest } from '../../../../services/api/generated/model/SpatialQueryRequest';
import { Marker, LatLng, FeatureGroup, TileLayer, Map, Icon } from 'leaflet';

@Component({
    selector: 'app-location-section-spatial-query',
    templateUrl: './location-section-spatial-query.component.html',
    styleUrls: ['./location-section-spatial-query.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class LocationSectionSpatialQueryComponent implements OnInit {

    title: String = 'Section Spatial Location Query';

    iri: string = null;

    radius: number = null;

    results: Array<SpatialQueryResult> = null;

    markerLayer: FeatureGroup = null;

    constructor(
        private documentService: DocumentService,
        private locationService: LocationService) { }

    options = null;

    ngOnInit() {
    }

    onMapReady(map: Map) {
        map.fitBounds(this.markerLayer.getBounds().pad(0.5));
    }

    onSubmit() {
        this.results = null;
        this.locationService.sectionSpatialQuery(this.iri, this.radius).subscribe(res => {
            const markers: Array<Marker> = [];
            for (const result of res) {
                markers.push(new Marker(new LatLng(result.latitude, result.longitude), {
                    icon: new Icon({
                        iconSize: [25, 41],
                        iconAnchor: [13, 0],
                        iconUrl: 'assets/marker-icon.png',
                        shadowUrl: 'assets/marker-shadow.png'
                    })
                }).bindPopup(result.iri));
            }
            this.markerLayer = new FeatureGroup(markers);
            this.options = {
                layers: [
                    new TileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 18, attribution: 'test' }),
                    this.markerLayer
                ],
                zoom: 5,
                center: this.markerLayer.getBounds().getCenter()
            };
            this.results = res;
        },
            err => {
                console.log('Error loading Locations!');
            });
    }

}
