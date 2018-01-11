import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterDocumentsComponent } from './filter-documents.component';

describe('FilterDocumentsComponent', () => {
  let component: FilterDocumentsComponent;
  let fixture: ComponentFixture<FilterDocumentsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FilterDocumentsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterDocumentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
