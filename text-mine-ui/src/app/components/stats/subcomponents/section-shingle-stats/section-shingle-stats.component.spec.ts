import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SectionShingleStatsComponent } from './section-shingle-stats.component';

describe('SectionShingleStatsComponent', () => {
  let component: SectionShingleStatsComponent;
  let fixture: ComponentFixture<SectionShingleStatsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SectionShingleStatsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SectionShingleStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
