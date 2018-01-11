import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonPageQueryComponent } from './person-page-query.component';

describe('PersonPageQueryComponent', () => {
  let component: PersonPageQueryComponent;
  let fixture: ComponentFixture<PersonPageQueryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PersonPageQueryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonPageQueryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
