import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {OverviewComponent} from "./overview/overview.component";
import {FwupdateComponent} from "./fwupdate/fwupdate.component";
import {ConfigComponent} from "./config/config.component";
import {HostComponent} from "./host.component";

const routes: Routes = [
    {
        path: "host/:id", component: HostComponent, children: [
        {path: "overview", component: OverviewComponent},
        {path: "fwupdate", component: FwupdateComponent},
        {path: "config", component: ConfigComponent},
    ]
    }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class HostRoutingModule { }
