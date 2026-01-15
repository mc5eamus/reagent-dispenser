import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Well, Plate } from '../../../../shared/models/plate.model';
import { Reagent } from '../../../../shared/models/reagent.model';
import { PlannedOperation, PlannedOperationStatus } from '../../../../shared/models/planned-operation.model';

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
  @Output() operationPlanned = new EventEmitter<PlannedOperation>();

  dispenseForm!: FormGroup;
  error: string | null = null;
  success = false;

  constructor(private fb: FormBuilder) {}

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

    this.error = null;

    const reagentId = +this.dispenseForm.value.reagentId;
    const reagent = this.reagents.find(r => r.id === reagentId);

    const plannedOperation: PlannedOperation = {
      id: this.generateUUID(),
      wellPosition: this.well.position,
      wellId: this.well.id,
      reagentId: reagentId,
      reagentName: reagent?.name,
      volume: +this.dispenseForm.value.volume,
      status: PlannedOperationStatus.PLANNED
    };

    this.success = true;
    
    // Emit the planned operation
    this.operationPlanned.emit(plannedOperation);
    
    // Auto-close after 1 second
    setTimeout(() => {
      this.close.emit();
    }, 1000);
  }

  private generateUUID(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0;
      const v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  onClose(): void {
    this.close.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.onClose();
    }
  }
}
