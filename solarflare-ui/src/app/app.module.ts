import "./rxjs-extensions";
import {ErrorHandler, NgModule} from "@angular/core";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpModule} from "@angular/http";
import {ClarityModule} from "clarity-angular";
import {AppRoutingModule, routedComponents} from "./app-routing.module";
import {EchoService, NavService} from "./services/index";
import {Globals} from "./shared/globals.service";
import {AppAlertService, GlobalsService, I18nService, RefreshService} from "./shared/index";
import {ActionDevService} from "./services/testing/action-dev.service";
import {DialogBoxComponent} from "./shared/dev/dialog-box.component";
import {DynamicDialogComponent} from "./shared/dev/dynamic-dialog.component";
import {AppErrorHandler} from "./shared/appErrorHandler";
import {AppComponent} from "./app.component";
import {SettingsModule} from "./views/settings/settings.module";
import {SharedModule} from "./shared/shared.module";
import {UserSettingService} from "app/shared/user-settings.service";
import {HostModule} from "./views/host/host.module";
import {HttpClient} from "@angular/common/http";
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
