import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonSectionQueryComponent } from './person-section-query.component';

describe('PersonSectionQueryComponent', () => {
  let component: PersonSectionQueryComponent;
  let fixture: ComponentFixture<PersonSectionQueryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PersonSectionQueryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonSectionQueryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
