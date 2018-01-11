import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { PersonService } from '../../../../services/person-service/person.service';

@Component({
    selector: 'app-person-exists',
    templateUrl: './person-exists.component.html',
    styleUrls: ['./person-exists.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class PersonExistsComponent implements OnInit {

    title = 'Check Person';

    person: string = null;

    exists: boolean = null;

    constructor(private personService: PersonService) { }

    ngOnInit() {
    }

    onSubmit() {
        this.exists = null;
        this.personService.exists(this.person).subscribe(res => {
            if (res === true) {
                this.exists = res;
            } else {
                this.exists = false;
            }
            console.log(res);
        });
    }
}
