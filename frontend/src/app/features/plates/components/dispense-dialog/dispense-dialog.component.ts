import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Well, Plate } from '../../../../shared/models/plate.model';
import { Reagent } from '../../../../shared/models/reagent.model';
import { DispenseService } from '../../../../core/services/dispense.service';
import { DispenseRequest } from '../../../../shared/models/dispense-operation.model';

@Component({
  selector: 'app-dispense-dialog',
  templateUrl: './dispense-dialog.component.html',
  styleUrls: ['./dispense-dialog.component.css']
})
export class DispenseDialogComponent implements OnInit {
  @Input() well!: Well;
  @Input() plate!: Plate;
  @Input() reagents: Reagent[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() dispenseComplete = new EventEmitter<void>();

  dispenseForm!: FormGroup;
  processing = false;
  error: string | null = null;
  success = false;

  constructor(
    private fb: FormBuilder,
    private dispenseService: DispenseService
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  initForm(): void {
    const availableVolume = this.well.maxVolume - (this.well.volume || 0);
    
    this.dispenseForm = this.fb.group({
      reagentId: ['', Validators.required],
      volume: [
        '',
        [
          Validators.required,
          Validators.min(0.1),
          Validators.max(availableVolume)
        ]
      ]
    });
  }

  getAvailableVolume(): number {
    return this.well.maxVolume - (this.well.volume || 0);
  }

  getSelectedReagent(): Reagent | undefined {
    const reagentId = this.dispenseForm.get('reagentId')?.value;
    return this.reagents.find(r => r.id === +reagentId);
  }

  onSubmit(): void {
    if (this.dispenseForm.invalid) {
      return;
    }

    this.processing = true;
    this.error = null;

    const request: DispenseRequest = {
      plateBarcode: this.plate.barcode,
      wellPosition: this.well.position,
      reagentId: +this.dispenseForm.value.reagentId,
      volume: +this.dispenseForm.value.volume
    };

    this.dispenseService.createOperation(request).subscribe({
      next: (operation) => {
        console.log('Dispense operation created:', operation);
        this.success = true;
        this.processing = false;
        
        // Auto-close after 2 seconds
        setTimeout(() => {
          this.dispenseComplete.emit();
        }, 2000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to dispense reagent: ' + err.message;
        this.processing = false;
        console.error('Dispense error:', err);
      }
    });
  }

  onClose(): void {
    if (!this.processing) {
      this.close.emit();
    }
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.onClose();
    }
  }
}
