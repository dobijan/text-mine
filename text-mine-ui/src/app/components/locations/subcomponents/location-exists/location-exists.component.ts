import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { LocationService } from '../../../../services/location-service/location.service';

@Component({
    selector: 'app-location-exists',
    templateUrl: './location-exists.component.html',
    styleUrls: ['./location-exists.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class LocationExistsComponent implements OnInit {

    title = 'Check Location';

    location: string = null;

    exists: boolean = null;

    constructor(private locationService: LocationService) { }

    ngOnInit() {
    }

    onSubmit() {
        this.exists = null;
        this.locationService.exists(this.location).subscribe(res => {
            if (res === true) {
                this.exists = res;
            } else {
                this.exists = false;
            }
            console.log(res);
        });
    }
}
