import "./rxjs-extensions";

import { NgModule, ErrorHandler }      from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpModule} from "@angular/http";
import {ClarityModule} from "clarity-angular";
import {AppRoutingModule, routedComponents} from "./app-routing.module";

import { EchoService, HostService, NavService }  from "./services/index";
import {Globals} from "./shared/globals.service";
import { GlobalsService, I18nService,
         AppAlertService, RefreshService }   from "./shared/index";
import {ActionDevService} from "./services/testing/action-dev.service";
import {DialogBoxComponent} from "./shared/dev/dialog-box.component";
import {DynamicDialogComponent} from "./shared/dev/dynamic-dialog.component";
import {AppErrorHandler} from "./shared/appErrorHandler";
import {AppComponent} from "./app.component";
import {SettingsModule} from "./views/settings/settings.module";
import {SharedModule} from "./shared/shared.module";

// [removable-chassis-code]
import { ChassisService } from "./services/chassis/chassis.service";
import { InMemoryWebApiModule, InMemoryBackendConfigArgs } from "angular-in-memory-web-api";
import { InMemoryDataService } from "./services/chassis/in-memory-data.service";
// [end-chassis-code]
import {UserSettingService} from "app/shared/user-settings.service";
import {HostModule} from "./views/host/host.module";
import {HTTP_INTERCEPTORS, HttpClient, HttpHandler} from "@angular/common/http";
import {AppMainService} from "./services/app-main.service";


@NgModule({
   imports: [
      BrowserModule,
      BrowserAnimationsModule,
      ClarityModule.forRoot(),
      HttpModule,
      AppRoutingModule,
      SettingsModule,
      SharedModule,
      // [removable-chassis-code]
      // InMemoryDataService config: forward unrecognized requests + remove the default 500ms delay
      InMemoryWebApiModule.forRoot(InMemoryDataService, <InMemoryBackendConfigArgs>{
         passThruUnknownUrl: true,
         delay: 0
      }),
      // [end-chassis-code]
       HostModule
   ],
   declarations: [
      AppComponent,
      DialogBoxComponent,
      DynamicDialogComponent,
      routedComponents
   ],
   providers: [
      ActionDevService,
      AppAlertService,
      AppErrorHandler,
      ChassisService, // [removable-chassis-line]
      EchoService,
      {provide: ErrorHandler, useClass: AppErrorHandler},
      Globals,
      GlobalsService,
      I18nService,
      NavService,
      RefreshService,
      UserSettingService,
       HttpClient,
       AppMainService
   ],
   bootstrap: [AppComponent]
})

export class AppModule {
}
