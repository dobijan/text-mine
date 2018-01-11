import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DocumentsByCorpusComponent } from './documents-by-corpus.component';

describe('DocumentsByCorpusComponent', () => {
  let component: DocumentsByCorpusComponent;
  let fixture: ComponentFixture<DocumentsByCorpusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DocumentsByCorpusComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DocumentsByCorpusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
