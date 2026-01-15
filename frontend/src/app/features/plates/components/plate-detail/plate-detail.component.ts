import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { PlateService } from '../../../../core/services/plate.service';
import { WebSocketService } from '../../../../core/services/websocket.service';
import { Plate, Well } from '../../../../shared/models/plate.model';
import { Reagent } from '../../../../shared/models/reagent.model';
import { ReagentService } from '../../../../core/services/reagent.service';

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
  
  private wsSubscription: Subscription | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private plateService: PlateService,
    private reagentService: ReagentService,
    private webSocketService: WebSocketService
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
          // Reload wells when dispense operation completes
          if (message.type === 'OPERATION_STATUS_CHANGE' && this.plate?.id) {
            const payload = message.payload as any;
            if (payload.plateId === this.plate.id) {
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

  onDispenseComplete(): void {
    this.showDispenseDialog = false;
    this.selectedWell = null;
    // Wells will be updated via WebSocket subscription
  }

  goBack(): void {
    this.router.navigate(['/plates']);
  }
}
