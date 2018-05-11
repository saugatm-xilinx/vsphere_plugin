import { Component, ViewChildren, ElementRef, QueryList, AfterViewInit } from "@angular/core";
import { Observable } from "rxjs/Observable";

import { GlobalsService, RefreshService,
         AppAlertService}   from "../index";
import { HostService } from "../../services/index";
import { NavService } from "../../services/nav.service";
import { UserSettingService } from "app/shared/user-settings.service";
import { HostList } from "../../services/host.service";
// [removable-chassis-code]
import { ChassisService, ChassisList } from "../../services/chassis/chassis.service";
// [end-chassis-code]

/**
 * Sidenav component visible only in plugin mode and shared by all views
 */
@Component({
   selector: "sidenav",
   styleUrls: ["./sidenav.component.scss"],
   templateUrl: "./sidenav.component.html"
})
export class SidenavComponent  implements AfterViewInit {
   hostList$: Observable<HostList>;
   selectedHost = -1;
   @ViewChildren("hostsInput") hostListInput: QueryList<ElementRef>;

   // [removable-chassis-code]
   chassisList$: Observable<ChassisList>;
   selectedChassis = -1;
   @ViewChildren("chassisInput") chassisListInput: QueryList<ElementRef>;
   // [end-chassis-code]

   constructor(public gs: GlobalsService,
               private appAlertService: AppAlertService,
               private chassisService: ChassisService,      // [removable-chassis-line]
               private hostService: HostService,
               public navService: NavService,
               private refreshService: RefreshService,
               private userSettingService: UserSettingService) {
      this.initObservables();
   }

   // Initialize the observables representing the object lists. No services are called here,
   // they will be called only when observables are subscribed to by using the "async" pipe.
   private initObservables(): void {
      this.hostList$ = this.refreshService.refreshObservable$
            .switchMap(() => this.hostService.getHosts())
            .do(hostList => {
               if (hostList.error) {
                  this.appAlertService.showError(hostList.error);
               } else if (hostList.hosts.length === 0) {
                  this.appAlertService.showInfo("No hosts found");
               }
            });
      // [removable-chassis-code]
      this.chassisList$ = this.refreshService.refreshObservable$
            .switchMap(() => this.chassisService.getChassisList(true))
            .do(chassisList => {
               if (chassisList.error) {
                  this.appAlertService.showError(chassisList.error);
               }
            });
      // [end-chassis-code]
   }

   ngAfterViewInit(): void {
      // Restore the lists' collapsed state from local settings.  Only hosts are shown initially.
      // @ViewChildren hostListInput allows to detect when the <input #hostsInput> element is
      // created in the DOM, i.e. after *ngIf="hostList$.." becomes true in the HTML template

      const hostsListCollapsed: boolean = (this.userSettingService.getSetting("showHosts") === "false");
      this.hostListInput.changes.subscribe(elements => {
         if (elements.first) {
            elements.first.nativeElement.checked = hostsListCollapsed;
         }
      });

      // [removable-chassis-code]
      const chassisListCollapsed: boolean = !(this.userSettingService.getSetting("showChassis") === "true");
      this.chassisListInput.changes.subscribe(elements => {
         if (elements.first) {
            elements.first.nativeElement.checked = chassisListCollapsed;
         }
      });
      // [end-chassis-code]
   }

   selectHost(hostId, index): void {
      this.selectedHost = index;
      this.selectedChassis = -1;  // [removable-chassis-line]
      this.navService.showObjectView(hostId, "host");
   }

   // [removable-chassis-code]
   selectChassis(chassisId, index): void {
      this.selectedHost = -1;
      this.selectedChassis = index;
      this.navService.showObjectView(chassisId, "chassis", "summary");
   }
   // [end-chassis-code]

   saveSetting(event): void {
      const key = "show" + event.target.id;
      this.userSettingService.setSetting(key, !event.target.checked);
   }

}

