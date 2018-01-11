import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationSectionSpatialQueryComponent } from './location-section-spatial-query.component';

describe('LocationSectionSpatialQueryComponent', () => {
  let component: LocationSectionSpatialQueryComponent;
  let fixture: ComponentFixture<LocationSectionSpatialQueryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationSectionSpatialQueryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationSectionSpatialQueryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
