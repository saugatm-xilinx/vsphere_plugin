import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {AdapterRoutingModule} from './adapter-routing.module';
import {AdapterComponent} from './adapter.component';
import {OverviewComponent} from './overview/overview.component';
import {FwupdateComponent} from './fwupdate/fwupdate.component';

import {ClrAlertModule, ClrFormsModule, ClrTabsModule} from "clarity-angular";
import {ClrModalModule} from "clarity-angular";
import {ClrDatagridModule} from "clarity-angular";
import {ClrIconModule} from "clarity-angular";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AdapterService} from "../../services/adapter.service";

@NgModule({
  imports: [
    CommonModule,
    AdapterRoutingModule,
      ClrTabsModule,
      ClrModalModule,
      ClrDatagridModule,
      ClrIconModule,
      FormsModule,
      ReactiveFormsModule,
      ClrAlertModule,
      ClrFormsModule
  ],
  declarations: [AdapterComponent, OverviewComponent, FwupdateComponent],
    providers: [
        AdapterService
    ]
})
export class AdapterModule { }
