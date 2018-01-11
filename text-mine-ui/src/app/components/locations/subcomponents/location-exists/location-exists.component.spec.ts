import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationExistsComponent } from './location-exists.component';

describe('LocationExistsComponent', () => {
  let component: LocationExistsComponent;
  let fixture: ComponentFixture<LocationExistsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationExistsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationExistsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
