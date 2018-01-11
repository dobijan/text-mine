import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationSectionQueryComponent } from './location-section-query.component';

describe('LocationSectionQueryComponent', () => {
  let component: LocationSectionQueryComponent;
  let fixture: ComponentFixture<LocationSectionQueryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationSectionQueryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationSectionQueryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
