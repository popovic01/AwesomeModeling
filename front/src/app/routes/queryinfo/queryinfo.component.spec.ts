import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QueryinfoComponent } from './queryinfo.component';

describe('QueryinfoComponent', () => {
  let component: QueryinfoComponent;
  let fixture: ComponentFixture<QueryinfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QueryinfoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QueryinfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
