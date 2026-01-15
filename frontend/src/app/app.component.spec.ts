import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';
import { WebSocketService } from './core/services/websocket.service';
import { of } from 'rxjs';

describe('AppComponent', () => {
  let mockWebSocketService: jasmine.SpyObj<WebSocketService>;

  beforeEach(async () => {
    mockWebSocketService = jasmine.createSpyObj('WebSocketService', [
      'connect',
      'disconnect',
      'isConnected'
    ]);
    mockWebSocketService.isConnected.and.returnValue(of(false));

    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule
      ],
      declarations: [
        AppComponent
      ],
      providers: [
        { provide: WebSocketService, useValue: mockWebSocketService }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'Reagent Dispenser'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('Reagent Dispenser');
  });

  it('should connect to WebSocket on init', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(mockWebSocketService.connect).toHaveBeenCalled();
  });

  it('should disconnect from WebSocket on destroy', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    fixture.destroy();
    expect(mockWebSocketService.disconnect).toHaveBeenCalled();
  });
});
