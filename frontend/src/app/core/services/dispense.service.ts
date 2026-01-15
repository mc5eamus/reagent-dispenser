import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DispenseOperation, DispenseRequest } from '../../shared/models/dispense-operation.model';

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
}
