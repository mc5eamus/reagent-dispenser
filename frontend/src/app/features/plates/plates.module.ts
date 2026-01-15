import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { PlateListComponent } from './components/plate-list/plate-list.component';
import { PlateDetailComponent } from './components/plate-detail/plate-detail.component';
import { WellGridComponent } from './components/well-grid/well-grid.component';
import { DispenseDialogComponent } from './components/dispense-dialog/dispense-dialog.component';

const routes: Routes = [
  { path: '', component: PlateListComponent },
  { path: ':id', component: PlateDetailComponent }
];

@NgModule({
  declarations: [
    PlateListComponent,
    PlateDetailComponent,
    WellGridComponent,
    DispenseDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class PlatesModule { }
