import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ArticlesByIdComponent } from './articles-by-id.component';

describe('ArticlesByIdComponent', () => {
  let component: ArticlesByIdComponent;
  let fixture: ComponentFixture<ArticlesByIdComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ArticlesByIdComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ArticlesByIdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
