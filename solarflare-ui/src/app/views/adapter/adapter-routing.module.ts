import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { OverviewComponent} from "./overview/overview.component";
import { FwupdateComponent} from "./fwupdate/fwupdate.component";
import {AdapterComponent} from "./adapter.component";

const routes: Routes = [
    { path: "adapter/:hostid/:adapterid", redirectTo: "overview", pathMatch: "full" },

    {

        path: "adapter/:hostid/:adapterid", component: AdapterComponent, children: [
        {path: "overview", component: OverviewComponent},
        {path: "fwupdate", component: FwupdateComponent},
    ]
    }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdapterRoutingModule { }
