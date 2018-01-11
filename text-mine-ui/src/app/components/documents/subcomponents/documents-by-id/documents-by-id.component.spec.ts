import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DocumentsByIdComponent } from './documents-by-id.component';

describe('DocumentsByIdComponent', () => {
  let component: DocumentsByIdComponent;
  let fixture: ComponentFixture<DocumentsByIdComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DocumentsByIdComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DocumentsByIdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
