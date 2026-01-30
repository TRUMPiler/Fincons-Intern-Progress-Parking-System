import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShowInfo } from './show-info';

describe('ShowInfo', () => {
  let component: ShowInfo;
  let fixture: ComponentFixture<ShowInfo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShowInfo]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShowInfo);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
