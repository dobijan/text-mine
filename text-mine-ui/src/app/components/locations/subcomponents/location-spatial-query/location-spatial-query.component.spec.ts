import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationSpatialQueryComponent } from './location-spatial-query.component';

describe('LocationSpatialQueryComponent', () => {
  let component: LocationSpatialQueryComponent;
  let fixture: ComponentFixture<LocationSpatialQueryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationSpatialQueryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationSpatialQueryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
