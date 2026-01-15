import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay, switchMap, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { DispenseOperation, DispenseRequest } from '../../shared/models/dispense-operation.model';
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

  /**
   * Execute a batch of planned operations sequentially with a 0.5s delay between each operation
   * @param plateBarcode The barcode of the plate for all operations
   * @param operations Array of planned operations to execute
   * @param onProgress Callback function to report progress for each operation
   * @returns Observable that emits when all operations are complete
   */
  executeBatch(
    plateBarcode: string,
    operations: PlannedOperation[],
    onProgress: (operation: PlannedOperation) => void
  ): Observable<void> {
    return this.executeBatchSequentially(plateBarcode, operations, 0, onProgress);
  }

  private executeBatchSequentially(
    plateBarcode: string,
    operations: PlannedOperation[],
    index: number,
    onProgress: (operation: PlannedOperation) => void
  ): Observable<void> {
    if (index >= operations.length) {
      return of(undefined);
    }

    const operation = operations[index];
    
    // Update status to EXECUTING
    operation.status = PlannedOperationStatus.EXECUTING;
    onProgress(operation);

    const request: DispenseRequest = {
      plateBarcode: plateBarcode,
      wellPosition: operation.wellPosition,
      reagentId: operation.reagentId,
      volume: operation.volume
    };

    // Execute the operation
    return this.createOperation(request).pipe(
      delay(500), // 0.5 second delay after execution
      switchMap(() => {
        // Update status to COMPLETED
        operation.status = PlannedOperationStatus.COMPLETED;
        onProgress(operation);
        
        // Continue with next operation
        return this.executeBatchSequentially(plateBarcode, operations, index + 1, onProgress);
      }),
      catchError((error) => {
        // Update status to FAILED
        operation.status = PlannedOperationStatus.FAILED;
        operation.error = error.error?.message || error.message || 'Operation failed';
        onProgress(operation);
        
        // Continue with next operation despite error
        return this.executeBatchSequentially(plateBarcode, operations, index + 1, onProgress);
      })
    );
  }
}
