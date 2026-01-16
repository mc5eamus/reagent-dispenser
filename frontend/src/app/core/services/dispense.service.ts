import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay, switchMap, catchError, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { 
  DispenseOperation, 
  DispenseRequest, 
  CreateBatchRequest, 
  AddOperationToBatchRequest, 
  DispenseBatch 
} from '../../shared/models/dispense-operation.model';
import { PlannedOperation, PlannedOperationStatus } from '../../shared/models/planned-operation.model';

@Injectable({
  providedIn: 'root'
})
export class DispenseService {
  private apiUrl = `${environment.apiUrl}/api/dispense`;

  constructor(private http: HttpClient) {}

  getHistory(): Observable<DispenseOperation[]> {
    return this.http.get<DispenseOperation[]>(`${this.apiUrl}/history`);
  }

  getById(id: number): Observable<DispenseOperation> {
    return this.http.get<DispenseOperation>(`${this.apiUrl}/${id}`);
  }

  getByStatus(status: string): Observable<DispenseOperation[]> {
    return this.http.get<DispenseOperation[]>(`${this.apiUrl}/status/${status}`);
  }

  createOperation(request: DispenseRequest): Observable<DispenseOperation> {
    return this.http.post<DispenseOperation>(this.apiUrl, request);
  }

  executeOperation(id: number): Observable<DispenseOperation> {
    return this.http.post<DispenseOperation>(`${this.apiUrl}/${id}/execute`, {});
  }

  // Batch API methods
  createBatch(request: CreateBatchRequest): Observable<DispenseBatch> {
    return this.http.post<DispenseBatch>(`${this.apiUrl}/batch`, request);
  }

  addOperationToBatch(batchId: number, request: AddOperationToBatchRequest): Observable<DispenseBatch> {
    return this.http.post<DispenseBatch>(`${this.apiUrl}/batch/${batchId}/add-operation`, request);
  }

  executeBatchById(batchId: number): Observable<DispenseBatch> {
    return this.http.post<DispenseBatch>(`${this.apiUrl}/batch/${batchId}/execute`, {});
  }

  getBatchById(batchId: number): Observable<DispenseBatch> {
    return this.http.get<DispenseBatch>(`${this.apiUrl}/batch/${batchId}`);
  }

  getAllBatches(): Observable<DispenseBatch[]> {
    return this.http.get<DispenseBatch[]>(`${this.apiUrl}/batch`);
  }

  /**
   * Execute a batch of planned operations using the backend batch API
   * @param plateBarcode The barcode of the plate for all operations
   * @param operations Array of planned operations to execute
   * @param onProgress Callback function to report progress for each operation
   * @returns Observable that emits when batch creation and execution is complete
   */
  executeBatch(
    plateBarcode: string,
    operations: PlannedOperation[],
    onProgress: (operation: PlannedOperation) => void
  ): Observable<void> {
    // Create a batch first
    return this.createBatch({ plateBarcode }).pipe(
      switchMap(batch => {
        console.log('Created batch:', batch);
        // Add all operations to the batch sequentially
        return this.addOperationsToBatch(batch.id, operations, 0).pipe(
          switchMap(() => {
            console.log('All operations added to batch, executing...');
            // Mark all operations as EXECUTING
            operations.forEach(op => {
              op.status = PlannedOperationStatus.EXECUTING;
              onProgress(op);
            });
            // Execute the batch
            return this.executeBatchById(batch.id);
          })
        );
      }),
      switchMap(() => of(undefined)), // Return void
      catchError((error) => {
        console.error('Batch execution error:', error);
        // Mark all operations as FAILED
        operations.forEach(op => {
          op.status = PlannedOperationStatus.FAILED;
          op.error = error.error?.message || error.message || 'Batch execution failed';
          onProgress(op);
        });
        return throwError(() => error);
      })
    );
  }

  /**
   * Recursively add operations to a batch
   */
  private addOperationsToBatch(
    batchId: number, 
    operations: PlannedOperation[], 
    index: number
  ): Observable<DispenseBatch> {
    if (index >= operations.length) {
      // All operations added, return the batch
      return this.getBatchById(batchId);
    }

    const operation = operations[index];
    const request: AddOperationToBatchRequest = {
      wellPosition: operation.wellPosition,
      reagentId: operation.reagentId,
      volume: operation.volume
    };

    return this.addOperationToBatch(batchId, request).pipe(
      switchMap(batch => {
        // Continue with next operation
        return this.addOperationsToBatch(batchId, operations, index + 1);
      })
    );
  }
}
