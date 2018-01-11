import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterPagesComponent } from './filter-pages.component';

describe('FilterPagesComponent', () => {
  let component: FilterPagesComponent;
  let fixture: ComponentFixture<FilterPagesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FilterPagesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterPagesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
