import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VehicleExit } from './vehicle-exit';

describe('VehicleExit', () => {
  let component: VehicleExit;
  let fixture: ComponentFixture<VehicleExit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VehicleExit]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VehicleExit);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
