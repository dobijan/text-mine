import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterLinesComponent } from './filter-lines.component';

describe('FilterLinesComponent', () => {
  let component: FilterLinesComponent;
  let fixture: ComponentFixture<FilterLinesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FilterLinesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterLinesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
