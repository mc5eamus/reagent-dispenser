import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: '/plates', pathMatch: 'full' },
  { 
    path: 'plates', 
    loadChildren: () => import('./features/plates/plates.module').then(m => m.PlatesModule)
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
