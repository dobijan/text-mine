import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PosShingleStatsComponent } from './pos-shingle-stats.component';

describe('PosShingleStatsComponent', () => {
  let component: PosShingleStatsComponent;
  let fixture: ComponentFixture<PosShingleStatsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PosShingleStatsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PosShingleStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
