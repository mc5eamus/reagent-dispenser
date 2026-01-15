import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PlateService } from '../../../../core/services/plate.service';
import { Plate } from '../../../../shared/models/plate.model';

@Component({
  selector: 'app-plate-list',
  templateUrl: './plate-list.component.html',
  styleUrls: ['./plate-list.component.css']
})
export class PlateListComponent implements OnInit {
  plates: Plate[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private plateService: PlateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPlates();
  }

  loadPlates(): void {
    this.loading = true;
    this.error = null;
    
    this.plateService.getAll().subscribe({
      next: (plates) => {
        this.plates = plates;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load plates: ' + err.message;
        this.loading = false;
        console.error('Error loading plates:', err);
      }
    });
  }

  viewPlate(plate: Plate): void {
    if (plate.id) {
      this.router.navigate(['/plates', plate.id]);
    }
  }

  getWellCount(plate: Plate): number {
    return plate.rows * plate.columns;
  }
}
