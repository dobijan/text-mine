import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SectionPosShingleStatsComponent } from './section-pos-shingle-stats.component';

describe('SectionPosShingleStatsComponent', () => {
  let component: SectionPosShingleStatsComponent;
  let fixture: ComponentFixture<SectionPosShingleStatsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SectionPosShingleStatsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SectionPosShingleStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
