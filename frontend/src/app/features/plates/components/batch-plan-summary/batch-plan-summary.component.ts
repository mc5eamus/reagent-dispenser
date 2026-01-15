import { Component, Input, Output, EventEmitter } from '@angular/core';
import { PlannedOperation, PlannedOperationStatus } from '../../../../shared/models/planned-operation.model';

@Component({
  selector: 'app-batch-plan-summary',
  templateUrl: './batch-plan-summary.component.html',
  styleUrls: ['./batch-plan-summary.component.css']
})
export class BatchPlanSummaryComponent {
  @Input() operations: PlannedOperation[] = [];
  @Output() removeOperation = new EventEmitter<string>();
  @Output() clearAll = new EventEmitter<void>();

  PlannedOperationStatus = PlannedOperationStatus;

  get totalOperations(): number {
    return this.operations.length;
  }

  get totalVolume(): number {
    return this.operations.reduce((sum, op) => sum + op.volume, 0);
  }

  get uniqueWells(): number {
    const wells = new Set(this.operations.map(op => op.wellPosition));
    return wells.size;
  }

  onRemove(operationId: string): void {
    this.removeOperation.emit(operationId);
  }

  onClearAll(): void {
    if (confirm('Are you sure you want to clear all planned operations?')) {
      this.clearAll.emit();
    }
  }

  getStatusClass(status: PlannedOperationStatus): string {
    switch (status) {
      case PlannedOperationStatus.PLANNED:
        return 'status-planned';
      case PlannedOperationStatus.EXECUTING:
        return 'status-executing';
      case PlannedOperationStatus.COMPLETED:
        return 'status-completed';
      case PlannedOperationStatus.FAILED:
        return 'status-failed';
      default:
        return '';
    }
  }

  getStatusIcon(status: PlannedOperationStatus): string {
    switch (status) {
      case PlannedOperationStatus.PLANNED:
        return '○';
      case PlannedOperationStatus.EXECUTING:
        return '⟳';
      case PlannedOperationStatus.COMPLETED:
        return '✓';
      case PlannedOperationStatus.FAILED:
        return '✗';
      default:
        return '';
    }
  }
}
