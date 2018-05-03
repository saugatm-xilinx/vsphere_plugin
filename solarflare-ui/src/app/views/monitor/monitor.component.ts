import { Component, OnInit, ViewChild } from "@angular/core";
import { Location }     from "@angular/common";
import { ActivatedRoute } from "@angular/router";
import { Observable } from "rxjs/Observable";

import { APP_CONFIG } from "../../shared/app-config";
import { GlobalsService, RefreshService, I18nService,
AppAlertService }   from "../../shared/index";
import { Host, HostError, HostService, NavService } from "../../services/index";
import { DialogBoxComponent }  from "../../shared/dev/dialog-box.component";
import { Action2WizardComponent } from "../modals/action2-wizard.component";

/**
 * View component for the Host monitor extension
 */
@Component({
   selector: "host-monitor",
   styleUrls: ["./monitor.component.scss"],
   templateUrl: "./monitor.component.html",
})
export class MonitorComponent implements OnInit {
   hostData$: Observable<Host | HostError>;
   titleKey: string;
   updateTime: string;
   // Note: use @ViewChildren in case you have more than one dialogBox.
   @ViewChild(DialogBoxComponent) dialogBox: DialogBoxComponent;
   @ViewChild(Action2WizardComponent) action2Wizard: Action2WizardComponent;

   constructor(public  gs: GlobalsService,
               private appAlertService: AppAlertService,
               private hostService: HostService,
               public  i18n: I18nService,
               private location: Location,
               public  nav: NavService,
               private refreshService: RefreshService,
               private route: ActivatedRoute) {

      // hostData$ is an Observable holding the data to be displayed in the view:
      // (see http://reactivex.io/rxjs/manual/overview.html#observable)
      // - it starts with refreshObservable$ in order to be triggered when Refresh is clicked
      //   (and also triggered once initially by the BehaviorSubject in RefreshService),
      // - then it maps to the route parameters from which the host id can be extracted,
      // - then it calls getHostProperties() to get the host data (switchMap allows to cancel
      //    async calls cleanly in case the host id changes before the data comes back,
      // - finally the .do operator performs some actions that don't affect the stream.
      //
      // NOTE: Observables can be declared in the constructor because they are "cold" objects,
      // i.e. nothing is executed here until someone subscribes to hostData$. This is done
      // by the "async" pipe in monitor.component.html when the content is rendered.
      // The async pipe also takes care of unsubscribing when the component is destroyed.

      this.hostData$ = this.refreshService.refreshObservable$
            .switchMap(() => this.route.paramMap)
            .map(paramMap => paramMap.get('id'))
            .switchMap(id => this.hostService.getHostProperties(id, APP_CONFIG.hostProperties))
            .do(hostData => {
               // .do allows to perform simple actions with no effect on the Observable stream
               // - updateTime is used to see that Refresh works by displaying the current time
               // - console.log is a common way to debug observable in a .do operator
               // - showError will display a message in the top banner in case of error
               this.updateTime = new Date().toLocaleTimeString();
               console.log("hostData = " + JSON.stringify(hostData));
               if (hostData instanceof HostError) {
                  this.appAlertService.showError((hostData as HostError).error)
               }
            });
   }

   ngOnInit(): void {
      // Initialize viewType since the same component is used for Monitor and Configure
      const viewType = /\/(.*)\//.exec(this.location.path())[1];
      this.nav.setViewType(viewType);
      this.titleKey = viewType + "View";
   }

   /**
    * Action1 dialog in dev mode
    * (in plugin mode the Action1 dialog will be opened from the Host menu)
    */
   public openAction1Dialog(host: Host): void {
      this.appAlertService.closeAlert();

      const title = "Action1 for " + host.name;
      this.dialogBox.openActionDialog(host, title);
   }

   /**
    * Action2 wizard in dev mode
    * (in plugin mode the Action2 wizard will be opened from the Host menu)
    */
   public openAction2Wizard(host: Host): void {
      this.appAlertService.closeAlert();

      const title = "Action2 wizard";
      this.action2Wizard.openWizard(host, title);
   }

   /**
    * Action3 is headless, so it only logs the call in dev mode
    */
   public callAction3(host: Host): void {
      this.appAlertService.closeAlert();

      // we set actionUid to same value as in plugin.xml
      const actionUid = APP_CONFIG.packageName + ".sampleAction3";
      const actionUrl = this.gs.getWebContextPath() + "/rest/actions.html?actionUid=" + actionUid;
      this.gs.getWebPlatform().callActionsController(actionUrl, null, host.id);
   }

   public getStatusIcon(host: Host): string {
      if (host.status === "green") {
         return "info";
      } else if (host.status === "yellow") {
         return "warning";
      }
      return "error";
   }

   showOtherView(id: string): void {
      this.nav.showObjectView(id, "host", (this.nav.getViewType() === 'monitor' ? 'manage' : 'monitor'));
   }
}
