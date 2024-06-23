import { Routes } from '@angular/router';
import { QueriesComponent } from './routes/queries/queries.component';
import { QueryinfoComponent } from './routes/queryinfo/queryinfo.component';

export const routes: Routes = [
    {path: "", redirectTo: "/query", pathMatch: "full"},
    {path: "query", component: QueriesComponent, pathMatch: "full"},
    {path: "query/:id", component: QueryinfoComponent}
];
