import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of, Subject } from 'rxjs';
import { PlateDetailComponent } from './plate-detail.component';
import { PlateService } from '../../../../core/services/plate.service';
import { WebSocketService } from '../../../../core/services/websocket.service';
import { DispenseService } from '../../../../core/services/dispense.service';
import { ReagentService } from '../../../../core/services/reagent.service';
import { Plate, Well } from '../../../../shared/models/plate.model';
import { WebSocketMessage } from '../../../../shared/models/websocket-message.model';

describe('PlateDetailComponent - WebSocket Well Volume Updates', () => {
  let component: PlateDetailComponent;
  let fixture: ComponentFixture<PlateDetailComponent>;
  let mockPlateService: jasmine.SpyObj<PlateService>;
  let mockReagentService: jasmine.SpyObj<ReagentService>;
  let mockDispenseService: jasmine.SpyObj<DispenseService>;
  let mockWebSocketService: jasmine.SpyObj<WebSocketService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let wsMessageSubject: Subject<WebSocketMessage>;

  const testPlate: Plate = {
    id: 1,
    barcode: 'TEST-PLATE-001',
    rows: 8,
    columns: 12,
    plateType: '96_WELL'
  };

  const testWells: Well[] = [
    { id: 1, position: 'A1', plateId: 1, volume: 50.0, maxVolume: 200.0 },
    { id: 2, position: 'A2', plateId: 1, volume: 0.0, maxVolume: 200.0 },
    { id: 3, position: 'B1', plateId: 1, volume: 100.0, maxVolume: 200.0 }
  ];

  beforeEach(async () => {
    // Create spies
    mockPlateService = jasmine.createSpyObj('PlateService', ['getById', 'getWells']);
    mockReagentService = jasmine.createSpyObj('ReagentService', ['getAll']);
    mockDispenseService = jasmine.createSpyObj('DispenseService', ['executeBatch']);
    mockWebSocketService = jasmine.createSpyObj('WebSocketService', ['subscribe']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    // Create a subject to simulate WebSocket messages
    wsMessageSubject = new Subject<WebSocketMessage>();
    mockWebSocketService.subscribe.and.returnValue(wsMessageSubject.asObservable());

    // Setup default return values with deep copies of test data
    mockPlateService.getById.and.returnValue(of({...testPlate}));
    mockPlateService.getWells.and.returnValue(of(testWells.map(w => ({...w}))));
    mockReagentService.getAll.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      declarations: [PlateDetailComponent],
      providers: [
        { provide: PlateService, useValue: mockPlateService },
        { provide: ReagentService, useValue: mockReagentService },
        { provide: DispenseService, useValue: mockDispenseService },
        { provide: WebSocketService, useValue: mockWebSocketService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '1'
              }
            }
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA] // Ignore child component errors
    }).compileComponents();

    fixture = TestBed.createComponent(PlateDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    wsMessageSubject.complete();
  });

  it('should update well volume directly from WebSocket payload', (done) => {
    // Arrange
    const initialWellVolume = component.wells.find(w => w.position === 'A1')?.volume;
    expect(initialWellVolume).toBe(50.0);

    // Prepare WebSocket message with updated well volume
    const wsMessage: WebSocketMessage = {
      type: 'OPERATION_STATUS_CHANGE',
      payload: {
        plateId: 1,
        wellPosition: 'A1',
        wellVolume: 75.0, // Updated volume after dispense
        reagentId: 1,
        status: 'COMPLETED'
      },
      timestamp: new Date().toISOString()
    };

    // Act - Send WebSocket message
    wsMessageSubject.next(wsMessage);

    // Assert - Well volume should be updated immediately
    setTimeout(() => {
      const updatedWell = component.wells.find(w => w.position === 'A1');
      expect(updatedWell).toBeDefined();
      expect(updatedWell?.volume).toBe(75.0);
      
      // Verify that wells were NOT reloaded (getWells should only be called once during init)
      expect(mockPlateService.getWells).toHaveBeenCalledTimes(1);
      done();
    }, 10);
  });

  it('should update multiple wells from multiple WebSocket messages', (done) => {
    // Arrange
    const well1InitialVolume = component.wells.find(w => w.position === 'A1')?.volume;
    const well2InitialVolume = component.wells.find(w => w.position === 'A2')?.volume;
    expect(well1InitialVolume).toBe(50.0);
    expect(well2InitialVolume).toBe(0.0);

    // Act - Send multiple WebSocket messages
    wsMessageSubject.next({
      type: 'OPERATION_STATUS_CHANGE',
      payload: {
        plateId: 1,
        wellPosition: 'A1',
        wellVolume: 75.0,
        reagentId: 1,
        status: 'COMPLETED'
      },
      timestamp: new Date().toISOString()
    });

    wsMessageSubject.next({
      type: 'OPERATION_STATUS_CHANGE',
      payload: {
        plateId: 1,
        wellPosition: 'A2',
        wellVolume: 50.0,
        reagentId: 2,
        status: 'COMPLETED'
      },
      timestamp: new Date().toISOString()
    });

    // Assert
    setTimeout(() => {
      const well1 = component.wells.find(w => w.position === 'A1');
      const well2 = component.wells.find(w => w.position === 'A2');
      
      expect(well1?.volume).toBe(75.0);
      expect(well2?.volume).toBe(50.0);
      
      // Verify that wells were NOT reloaded for each message
      expect(mockPlateService.getWells).toHaveBeenCalledTimes(1);
      done();
    }, 10);
  });

  it('should not update wells for different plate', (done) => {
    // Arrange
    const initialVolume = component.wells.find(w => w.position === 'A1')?.volume;

    // Act - Send WebSocket message for different plate
    wsMessageSubject.next({
      type: 'OPERATION_STATUS_CHANGE',
      payload: {
        plateId: 999, // Different plate ID
        wellPosition: 'A1',
        wellVolume: 150.0,
        reagentId: 1,
        status: 'COMPLETED'
      },
      timestamp: new Date().toISOString()
    });

    // Assert - Well volume should NOT be updated
    setTimeout(() => {
      const well = component.wells.find(w => w.position === 'A1');
      expect(well?.volume).toBe(initialVolume);
      done();
    }, 10);
  });

  it('should handle WebSocket message with undefined wellVolume gracefully', (done) => {
    // Arrange
    const initialVolume = component.wells.find(w => w.position === 'A1')?.volume;

    // Act - Send WebSocket message without wellVolume
    wsMessageSubject.next({
      type: 'OPERATION_STATUS_CHANGE',
      payload: {
        plateId: 1,
        wellPosition: 'A1',
        // wellVolume is undefined
        reagentId: 1,
        status: 'IN_PROGRESS'
      },
      timestamp: new Date().toISOString()
    });

    // Assert - Well volume should remain unchanged
    setTimeout(() => {
      const well = component.wells.find(w => w.position === 'A1');
      expect(well?.volume).toBe(initialVolume);
      done();
    }, 10);
  });

  it('should reload wells on BATCH_EXECUTION_COMPLETED', (done) => {
    // Arrange
    const callCountBeforeBatchComplete = mockPlateService.getWells.calls.count();

    // Act - Send BATCH_EXECUTION_COMPLETED message
    wsMessageSubject.next({
      type: 'BATCH_EXECUTION_COMPLETED',
      payload: {
        plateId: 1,
        status: 'COMPLETED'
      },
      timestamp: new Date().toISOString()
    });

    // Assert - Wells should be reloaded
    setTimeout(() => {
      expect(mockPlateService.getWells).toHaveBeenCalledTimes(callCountBeforeBatchComplete + 1);
      expect(component.isExecutingBatch).toBe(false);
      done();
    }, 10);
  });
});
