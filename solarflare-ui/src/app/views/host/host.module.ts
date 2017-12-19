import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { HostRoutingModule } from './host-routing.module';
import {HostComponent} from "./host.component";
import {OverviewComponent} from "./overview/overview.component";
import {FwupdateComponent} from "./fwupdate/fwupdate.component";
import {ConfigComponent} from "./config/config.component";
import {ClrTabsModule} from "clarity-angular";
import {RangePipe} from "../../range.pipe";
import {ClrModalModule} from "clarity-angular";
import {ClrDatagridModule} from "clarity-angular";
import {ClrIconModule} from "clarity-angular";

@NgModule({
  imports: [
    CommonModule,
    HostRoutingModule,
      ClrTabsModule,
      ClrModalModule,
      ClrDatagridModule,
      ClrIconModule
  ],
  declarations: [HostComponent, OverviewComponent, FwupdateComponent, ConfigComponent, RangePipe]
})
export class HostModule { }
