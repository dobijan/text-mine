import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PageShingleStatsComponent } from './page-shingle-stats.component';

describe('PageShingleStatsComponent', () => {
  let component: PageShingleStatsComponent;
  let fixture: ComponentFixture<PageShingleStatsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PageShingleStatsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageShingleStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
