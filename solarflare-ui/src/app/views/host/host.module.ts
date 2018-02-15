import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { HostRoutingModule } from './host-routing.module';
import {HostComponent} from "./host.component";
import {OverviewComponent} from "./overview/overview.component";
import {FwupdateComponent} from "./fwupdate/fwupdate.component";
import {ConfigComponent} from "./config/config.component";
import {ClrAlertModule, ClrFormsModule, ClrTabsModule} from "clarity-angular";
import {RangePipe} from "../../shared/pipes/range.pipe";
import {ClrModalModule} from "clarity-angular";
import {ClrDatagridModule} from "clarity-angular";
import {ClrIconModule} from "clarity-angular";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HostsService} from "../../services/hosts.service";

@NgModule({
  imports: [
    CommonModule,
    HostRoutingModule,
      ClrTabsModule,
      ClrModalModule,
      ClrDatagridModule,
      ClrIconModule,
      FormsModule,
      ReactiveFormsModule,
      ClrAlertModule,
      ClrFormsModule
  ],
  declarations: [HostComponent, OverviewComponent, FwupdateComponent, ConfigComponent, RangePipe],
    exports: [
        ReactiveFormsModule
    ],
    providers: [
        HostsService
    ]
})
export class HostModule { }
