import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Executions } from './executions';

describe('Executions', () => {
  let component: Executions;
  let fixture: ComponentFixture<Executions>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Executions],
    }).compileComponents();

    fixture = TestBed.createComponent(Executions);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
