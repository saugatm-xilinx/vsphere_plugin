import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BootParamsComponent } from './boot-params.component';

describe('BootParamsComponent', () => {
  let component: BootParamsComponent;
  let fixture: ComponentFixture<BootParamsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BootParamsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BootParamsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
