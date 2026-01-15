import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Reagent } from '../../shared/models/reagent.model';

@Injectable({
  providedIn: 'root'
})
export class ReagentService {
  private apiUrl = `${environment.apiUrl}/api/reagents`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Reagent[]> {
    return this.http.get<Reagent[]>(this.apiUrl);
  }

  getById(id: number): Observable<Reagent> {
    return this.http.get<Reagent>(`${this.apiUrl}/${id}`);
  }

  create(reagent: Reagent): Observable<Reagent> {
    return this.http.post<Reagent>(this.apiUrl, reagent);
  }

  update(id: number, reagent: Reagent): Observable<Reagent> {
    return this.http.put<Reagent>(`${this.apiUrl}/${id}`, reagent);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
