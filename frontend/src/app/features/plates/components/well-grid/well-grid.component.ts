import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Plate, Well } from '../../../../shared/models/plate.model';
import { PlannedOperation } from '../../../../shared/models/planned-operation.model';

@Component({
  selector: 'app-well-grid',
  templateUrl: './well-grid.component.html',
  styleUrls: ['./well-grid.component.css']
})
export class WellGridComponent {
  @Input() plate: Plate | null = null;
  @Input() wells: Well[] = [];
  @Input() plannedOperations: PlannedOperation[] = [];
  @Output() wellSelected = new EventEmitter<Well>();

  rowLabels: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P'];

  getWell(rowIndex: number, colIndex: number): Well | undefined {
    const position = this.getWellPosition(rowIndex, colIndex);
    return this.wells.find(w => w.position === position);
  }

  getWellPosition(rowIndex: number, colIndex: number): string {
    return `${this.rowLabels[rowIndex]}${colIndex + 1}`;
  }

  onWellClick(rowIndex: number, colIndex: number): void {
    const well = this.getWell(rowIndex, colIndex);
    if (well) {
      this.wellSelected.emit(well);
    }
  }

  getWellFillLevel(well: Well | undefined): number {
    if (!well || !well.volume) return 0;
    return (well.volume / well.maxVolume) * 100;
  }

  getWellColor(fillLevel: number): string {
    if (fillLevel === 0) return '#ffffff';
    if (fillLevel < 25) return '#e3f2fd';
    if (fillLevel < 50) return '#90caf9';
    if (fillLevel < 75) return '#42a5f5';
    if (fillLevel < 90) return '#1e88e5';
    return '#1565c0';
  }

  getWellTooltip(well: Well | undefined, position: string): string {
    if (!well) return `${position}: Empty`;
    const volume = well.volume?.toFixed(1) || '0.0';
    const max = well.maxVolume.toFixed(1);
    return `${position}: ${volume}/${max} μL`;
  }

  getRows(): number[] {
    return Array(this.plate?.rows || 8).fill(0).map((_, i) => i);
  }

  getColumns(): number[] {
    return Array(this.plate?.columns || 12).fill(0).map((_, i) => i);
  }

  hasPlannedOperation(position: string): boolean {
    return this.plannedOperations.some(op => op.wellPosition === position);
  }

  getWellBorderClass(well: Well | undefined, position: string): string {
    if (well && well.volume && well.volume > 0) {
      return 'well-filled'; // Green border for wells with volume
    }
    if (this.hasPlannedOperation(position)) {
      return 'well-planned'; // Yellow border for wells with planned operations
    }
    return '';
  }
}
