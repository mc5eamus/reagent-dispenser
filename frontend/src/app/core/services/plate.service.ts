import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Plate, Well } from '../../shared/models/plate.model';

@Injectable({
  providedIn: 'root'
})
export class PlateService {
  private apiUrl = `${environment.apiUrl}/api/plates`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Plate[]> {
    return this.http.get<Plate[]>(this.apiUrl);
  }

  getById(id: number): Observable<Plate> {
    return this.http.get<Plate>(`${this.apiUrl}/${id}`);
  }

  getByBarcode(barcode: string): Observable<Plate> {
    return this.http.get<Plate>(`${this.apiUrl}/barcode/${barcode}`);
  }

  getWells(plateId: number): Observable<Well[]> {
    return this.http.get<Well[]>(`${this.apiUrl}/${plateId}/wells`);
  }

  create(plate: Plate): Observable<Plate> {
    return this.http.post<Plate>(this.apiUrl, plate);
  }

  update(id: number, plate: Plate): Observable<Plate> {
    return this.http.put<Plate>(`${this.apiUrl}/${id}`, plate);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
