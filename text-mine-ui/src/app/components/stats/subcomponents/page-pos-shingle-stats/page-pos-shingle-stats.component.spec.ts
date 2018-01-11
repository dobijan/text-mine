import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PagePosShingleStatsComponent } from './page-pos-shingle-stats.component';

describe('PagePosShingleStatsComponent', () => {
  let component: PagePosShingleStatsComponent;
  let fixture: ComponentFixture<PagePosShingleStatsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PagePosShingleStatsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PagePosShingleStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
