import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationPageQueryComponent } from './location-page-query.component';

describe('LocationPageQueryComponent', () => {
  let component: LocationPageQueryComponent;
  let fixture: ComponentFixture<LocationPageQueryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationPageQueryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationPageQueryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
