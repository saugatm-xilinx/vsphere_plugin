import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FwupdateComponent } from './fwupdate.component';

describe('FwupdateComponent', () => {
  let component: FwupdateComponent;
  let fixture: ComponentFixture<FwupdateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FwupdateComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FwupdateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
