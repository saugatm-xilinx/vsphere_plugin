import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { OverviewComponent } from "./overview/overview.component";
import { FwupdateComponent } from "./fwupdate/fwupdate.component";
import { AdapterComponent } from "./adapter.component";

const routes: Routes = [
    { path: "adapter/:hostid/:nicId", redirectTo: "overview", pathMatch: "full" },

    {
        path: '', component: AdapterComponent, children: [
            { path: '', redirectTo: 'overview', pathMatch: 'full' },
            { path: 'overview', component: OverviewComponent, pathMatch: 'full' },
            { path: 'fwupdate', component: FwupdateComponent },
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AdapterRoutingModule { }
