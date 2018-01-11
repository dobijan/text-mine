import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupArticlesByDocumentComponent } from './group-articles-by-document.component';

describe('GroupArticlesByDocumentComponent', () => {
  let component: GroupArticlesByDocumentComponent;
  let fixture: ComponentFixture<GroupArticlesByDocumentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GroupArticlesByDocumentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupArticlesByDocumentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
