import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonLineQueryComponent } from './person-line-query.component';

describe('PersonLineQueryComponent', () => {
  let component: PersonLineQueryComponent;
  let fixture: ComponentFixture<PersonLineQueryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PersonLineQueryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonLineQueryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
