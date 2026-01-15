import { Component, Input } from '@angular/core';
import { PlannedOperation, PlannedOperationStatus } from '../../../../shared/models/planned-operation.model';

@Component({
  selector: 'app-batch-execution-status',
  templateUrl: './batch-execution-status.component.html',
  styleUrls: ['./batch-execution-status.component.css']
})
export class BatchExecutionStatusComponent {
  @Input() operations: PlannedOperation[] = [];
  @Input() isExecuting = false;

  PlannedOperationStatus = PlannedOperationStatus;

  get totalOperations(): number {
    return this.operations.length;
  }

  get completedOperations(): number {
    return this.operations.filter(op => op.status === PlannedOperationStatus.COMPLETED).length;
  }

  get failedOperations(): number {
    return this.operations.filter(op => op.status === PlannedOperationStatus.FAILED).length;
  }

  get executingOperation(): PlannedOperation | undefined {
    return this.operations.find(op => op.status === PlannedOperationStatus.EXECUTING);
  }

  get progressPercentage(): number {
    if (this.totalOperations === 0) return 0;
    const completed = this.completedOperations + this.failedOperations;
    return Math.round((completed / this.totalOperations) * 100);
  }

  get statusMessage(): string {
    if (!this.isExecuting) {
      if (this.completedOperations === this.totalOperations) {
        return 'All operations completed successfully!';
      } else if (this.failedOperations > 0) {
        return `Completed with ${this.failedOperations} failure(s)`;
      }
      return 'Ready to execute';
    }

    const remaining = this.totalOperations - this.completedOperations - this.failedOperations;
    return `Executing... ${remaining} operation(s) remaining`;
  }
}
