import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VehicleEntry } from './vehicle-entry';

describe('VehicleEntry', () => {
  let component: VehicleEntry;
  let fixture: ComponentFixture<VehicleEntry>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VehicleEntry]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VehicleEntry);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
