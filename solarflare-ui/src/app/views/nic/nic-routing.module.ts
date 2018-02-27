import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { NicComponent } from './nic.component';
import { OverviewComponent } from './overview/overview.component';
import { BootParamsComponent } from './boot-params/boot-params.component';
import { ConfigurationComponent } from './configuration/configuration.component';
import { DiagnosticsComponent } from './diagnostics/diagnostics.component';
import { StatisticsComponent } from './statistics/statistics.component';

const routes: Routes = [
    {
        path: '', component: NicComponent, children: [
            { path: '', redirectTo: 'overview', pathMatch: 'full' },
            { path: 'overview', component: OverviewComponent, pathMatch: 'full' },
            { path: 'boot-params', component: BootParamsComponent },
            { path: 'configuration', component: ConfigurationComponent },
            { path: 'diagnostics', component: DiagnosticsComponent },
            { path: 'statistics', component: StatisticsComponent },
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class NicRoutingModule { }
