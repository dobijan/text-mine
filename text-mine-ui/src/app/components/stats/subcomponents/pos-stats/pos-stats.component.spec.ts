import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PosStatsComponent } from './pos-stats.component';

describe('PosStatsComponent', () => {
  let component: PosStatsComponent;
  let fixture: ComponentFixture<PosStatsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PosStatsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PosStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
