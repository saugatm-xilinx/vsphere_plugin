import { Component, OnInit, Injector, ChangeDetectorRef } from "@angular/core";

import { GlobalsService, RefreshService, I18nService }   from "./shared/index";
import { ActionDevService } from "./services/testing/action-dev.service";
import { AppMainService} from "./services/app-main.service";
import {Http} from "@angular/http";

@Component({
   selector: "my-app",
   styleUrls: ["./app.component.scss"],
   templateUrl: "./app.component.html",
   providers: [ ]
})
export class AppComponent {
    public hosts: any;
    public allHostUrl = this.gs.getWebContextPath() + '/rest/services/hosts';

   constructor(public  gs: GlobalsService,
               private injector: Injector,
               private refreshService: RefreshService,
               private i18nService: I18nService,
               private changeDetector: ChangeDetectorRef,
               private appMainService: AppMainService,
               private http: Http) {

      // Refresh handler to be used in plugin mode
      this.gs.getWebPlatform().setGlobalRefreshHandler(this.refresh.bind(this), document);

      // Manual injection of ActionDevService, used in webPlatformStub
      if (!this.gs.isPluginMode()) {
         this.injector.get(ActionDevService);
      }

      // Start the app in english by default (dev mode)
      // In plugin mode the current locale is passed as parameter
      this.i18nService.initLocale("en");
   }

    ngOnInit(): void {
       let url = "";
       if (this.gs.isPluginMode()){
           url = this.allHostUrl;
       }else{
           url = 'https://10.101.10.7/ui/solarflare/rest/services/hosts/';
       }
        this.http.get(url)
            .subscribe(
                data => { this.hosts = data.json() },
                err => console.error(err)
            );
    }


    refresh(): void {
      // This propagates the refresh event to views that have subscribed to the RefreshService
      this.refreshService.refreshView();

      if (this.gs.isPluginMode()) {
         // This helps refresh the app's children components in Plugin mode after refreshView
         this.changeDetector.detectChanges();
      }
   }
}
