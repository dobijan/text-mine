import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonExistsComponent } from './person-exists.component';

describe('PersonExistsComponent', () => {
  let component: PersonExistsComponent;
  let fixture: ComponentFixture<PersonExistsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PersonExistsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonExistsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
