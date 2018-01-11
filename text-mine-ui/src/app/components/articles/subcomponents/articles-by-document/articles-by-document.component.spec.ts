import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ArticlesByDocumentComponent } from './articles-by-document.component';

describe('ArticlesByDocumentComponent', () => {
  let component: ArticlesByDocumentComponent;
  let fixture: ComponentFixture<ArticlesByDocumentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ArticlesByDocumentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ArticlesByDocumentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
