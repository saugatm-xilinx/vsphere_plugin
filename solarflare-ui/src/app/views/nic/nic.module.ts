import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ClrAlertModule, ClrFormsModule, ClrTabsModule } from "clarity-angular";
import { RangePipe } from "../../shared/pipes/range.pipe";
import { ClrModalModule } from "clarity-angular";
import { ClrDatagridModule } from "clarity-angular";
import { ClrIconModule } from "clarity-angular";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { NicRoutingModule } from './nic-routing.module';
import { OverviewComponent } from './overview/overview.component';
import { StatisticsComponent } from './statistics/statistics.component';
import { BootParamsComponent } from './boot-params/boot-params.component';
import { DiagnosticsComponent } from './diagnostics/diagnostics.component';
import { ConfigurationComponent } from './configuration/configuration.component';
import { NicComponent } from './nic.component';

@NgModule({
    imports: [
        CommonModule,
        NicRoutingModule,
        ClrTabsModule,
        ClrModalModule,
        ClrDatagridModule,
        ClrIconModule,
        FormsModule,
        ReactiveFormsModule,
        ClrAlertModule,
        ClrFormsModule
    ],
    declarations: [
        NicComponent,
        OverviewComponent,
        StatisticsComponent,
        BootParamsComponent,
        DiagnosticsComponent,
        ConfigurationComponent
    ],
    exports: [
        ReactiveFormsModule
    ],
    providers: []
})
export class NicModule { }
