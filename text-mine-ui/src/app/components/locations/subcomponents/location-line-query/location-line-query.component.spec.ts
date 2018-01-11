import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationLineQueryComponent } from './location-line-query.component';

describe('LocationLineQueryComponent', () => {
  let component: LocationLineQueryComponent;
  let fixture: ComponentFixture<LocationLineQueryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationLineQueryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationLineQueryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
