import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NotesByDocumentComponent } from './notes-by-document.component';

describe('NotesByDocumentComponent', () => {
  let component: NotesByDocumentComponent;
  let fixture: ComponentFixture<NotesByDocumentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NotesByDocumentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NotesByDocumentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
