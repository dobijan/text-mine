import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NotesByIdComponent } from './notes-by-id.component';

describe('NotesByIdComponent', () => {
  let component: NotesByIdComponent;
  let fixture: ComponentFixture<NotesByIdComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NotesByIdComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NotesByIdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
