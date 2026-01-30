import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParkinglotRegisteration } from './parkinglot-registeration';

describe('ParkinglotRegisteration', () => {
  let component: ParkinglotRegisteration;
  let fixture: ComponentFixture<ParkinglotRegisteration>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ParkinglotRegisteration]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParkinglotRegisteration);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
