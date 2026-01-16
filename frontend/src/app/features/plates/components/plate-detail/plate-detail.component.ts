import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { PlateService } from '../../../../core/services/plate.service';
import { WebSocketService } from '../../../../core/services/websocket.service';
import { DispenseService } from '../../../../core/services/dispense.service';
import { Plate, Well } from '../../../../shared/models/plate.model';
import { Reagent } from '../../../../shared/models/reagent.model';
import { ReagentService } from '../../../../core/services/reagent.service';
import { PlannedOperation, PlannedOperationStatus } from '../../../../shared/models/planned-operation.model';

@Component({
  selector: 'app-plate-detail',
  templateUrl: './plate-detail.component.html',
  styleUrls: ['./plate-detail.component.css']
})
export class PlateDetailComponent implements OnInit, OnDestroy {
  plate: Plate | null = null;
  wells: Well[] = [];
  reagents: Reagent[] = [];
  loading = false;
  error: string | null = null;
  selectedWell: Well | null = null;
  showDispenseDialog = false;
  
  // Batch planning state
  plannedOperations: PlannedOperation[] = [];
  isExecutingBatch = false;
  
  private wsSubscription: Subscription | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private plateService: PlateService,
    private reagentService: ReagentService,
    private webSocketService: WebSocketService,
    private dispenseService: DispenseService
  ) {}

  ngOnInit(): void {
    const plateId = this.route.snapshot.paramMap.get('id');
    if (plateId) {
      this.loadPlate(+plateId);
      this.loadReagents();
      this.subscribeToUpdates();
    }
  }

  ngOnDestroy(): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
  }

  loadPlate(id: number): void {
    this.loading = true;
    this.error = null;

    this.plateService.getById(id).subscribe({
      next: (plate) => {
        this.plate = plate;
        this.loadWells(id);
      },
      error: (err) => {
        this.error = 'Failed to load plate: ' + err.message;
        this.loading = false;
        console.error('Error loading plate:', err);
      }
    });
  }

  loadWells(plateId: number): void {
    this.plateService.getWells(plateId).subscribe({
      next: (wells) => {
        this.wells = wells;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load wells: ' + err.message;
        this.loading = false;
        console.error('Error loading wells:', err);
      }
    });
  }

  loadReagents(): void {
    this.reagentService.getAll().subscribe({
      next: (reagents) => {
        this.reagents = reagents;
      },
      error: (err) => {
        console.error('Error loading reagents:', err);
      }
    });
  }

  subscribeToUpdates(): void {
    this.wsSubscription = this.webSocketService
      .subscribe('/topic/dispense-status')
      .subscribe({
        next: (message) => {
          console.log('WebSocket update:', message);
          
          if (!this.plate?.id) return;
          
          // Handle different message types
          if (message.type === 'OPERATION_STATUS_CHANGE') {
            const payload = message.payload as any;
            if (payload.plateId === this.plate.id) {
              // Update the planned operation status if it exists
              const plannedOp = this.plannedOperations.find(
                op => op.wellPosition === payload.wellPosition && 
                     op.reagentId === payload.reagentId
              );
              if (plannedOp) {
                if (payload.status === 'COMPLETED') {
                  plannedOp.status = PlannedOperationStatus.COMPLETED;
                } else if (payload.status === 'FAILED') {
                  plannedOp.status = PlannedOperationStatus.FAILED;
                  plannedOp.error = payload.errorMessage || 'Operation failed';
                }
              }
              
              // Reload wells to reflect volume changes
              this.loadWells(this.plate.id);
            }
          } else if (message.type === 'BATCH_EXECUTION_COMPLETED') {
            const payload = message.payload as any;
            if (payload.plateId === this.plate.id) {
              console.log('Batch execution completed');
              this.isExecutingBatch = false;
              // Final reload to ensure all changes are reflected
              this.loadWells(this.plate.id);
            }
          }
        },
        error: (err) => {
          console.error('WebSocket error:', err);
        }
      });
  }

  onWellSelected(well: Well): void {
    this.selectedWell = well;
    this.showDispenseDialog = true;
  }

  onDispenseDialogClose(): void {
    this.showDispenseDialog = false;
    this.selectedWell = null;
  }

  onOperationPlanned(operation: PlannedOperation): void {
    this.plannedOperations.push(operation);
    console.log('Operation added to plan:', operation);
  }

  onRemoveOperation(operationId: string): void {
    this.plannedOperations = this.plannedOperations.filter(op => op.id !== operationId);
  }

  onClearAllOperations(): void {
    this.plannedOperations = [];
  }

  onExecuteBatch(): void {
    if (this.plannedOperations.length === 0 || !this.plate) {
      return;
    }

    if (!confirm(`Execute ${this.plannedOperations.length} dispense operation(s)?`)) {
      return;
    }

    this.isExecutingBatch = true;

    // Create a copy of operations to execute
    const operationsToExecute = this.plannedOperations.map(op => ({ ...op }));

    this.dispenseService.executeBatch(this.plate.barcode, operationsToExecute, (operation) => {
      // Update the operation status in the planned operations list
      const index = this.plannedOperations.findIndex(op => op.id === operation.id);
      if (index !== -1) {
        this.plannedOperations[index] = { ...operation };
      }
    }).subscribe({
      next: () => {
        console.log('Batch execution request sent successfully');
        // Note: isExecutingBatch will be set to false when BATCH_EXECUTION_COMPLETED is received
      },
      error: (err) => {
        console.error('Batch execution error:', err);
        this.isExecutingBatch = false;
        this.error = 'Batch execution encountered an error: ' + err.message;
      }
    });
  }

  get canExecuteBatch(): boolean {
    return this.plannedOperations.length > 0 && 
           !this.isExecutingBatch &&
           this.plannedOperations.every(op => op.status === PlannedOperationStatus.PLANNED);
  }

  goBack(): void {
    this.router.navigate(['/plates']);
  }
}
