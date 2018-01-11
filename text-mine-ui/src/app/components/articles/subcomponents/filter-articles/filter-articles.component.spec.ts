import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterArticlesComponent } from './filter-articles.component';

describe('FilterArticlesComponent', () => {
  let component: FilterArticlesComponent;
  let fixture: ComponentFixture<FilterArticlesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FilterArticlesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterArticlesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
