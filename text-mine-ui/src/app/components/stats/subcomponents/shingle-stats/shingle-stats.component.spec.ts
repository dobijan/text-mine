import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ShingleStatsComponent } from './shingle-stats.component';

describe('ShingleStatsComponent', () => {
  let component: ShingleStatsComponent;
  let fixture: ComponentFixture<ShingleStatsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ShingleStatsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ShingleStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
