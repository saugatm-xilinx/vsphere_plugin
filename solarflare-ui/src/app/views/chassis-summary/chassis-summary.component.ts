import { Component, Input, OnInit, ViewChild } from "@angular/core";
import { ActivatedRoute, Params } from "@angular/router";
import { Observable } from "rxjs/Observable";

import { AppAlertService, GlobalsService, RefreshService }   from "../../shared/index";
import { Chassis, ChassisService, ChassisError }  from "../../services/chassis/index";
import { NavService } from "../../services/nav.service";
import { I18nService } from "../../shared/i18n.service";
import { DialogBoxComponent } from "../../shared/dev/dialog-box.component";

@Component({
   styleUrls: ["./chassis-summary.component.scss"],
   templateUrl: "./chassis-summary.component.html",
})
export class ChassisSummaryComponent implements OnInit {
   // Chassis object displayed in this view
   chassisData$: Observable<Chassis | ChassisError>;

   @Input() level1: number;
   @Input() level2: number;

   // Note: use @ViewChildren in case you have more than one dialogBox.
   @ViewChild(DialogBoxComponent) dialogBox: DialogBoxComponent;

   constructor(public gs: GlobalsService,
               public nav: NavService,
               private appAlertService: AppAlertService,
               private chassisService: ChassisService,
               public  i18n: I18nService,
               private refreshService: RefreshService,
               private route: ActivatedRoute) {

      this.chassisData$ = this.refreshService.refreshObservable$
            .switchMap(() => this.route.paramMap)
            .map(paramMap => paramMap.get('id'))
            .switchMap(id => this.chassisService.getChassis(id))
            .do(() => {
               // Mock health and compliance levels
               this.level1 = Math.round(Math.random() * 100);
               this.level2 = Math.round(Math.random() * 100);
            })
   }

   editChassis(chassis: Chassis): void {
      const title = this.i18n.translate("edit.chassis", chassis.name);

      if (this.gs.isPluginMode()) {
         const url = this.gs.getWebContextPath() +
               "/index.html?view=edit-chassis&actionUid=com.solarflare.vcp.editChassis";
         this.gs.getWebPlatform().openModalDialog(title, url,  576, 248, chassis.id);
      } else {
         this.dialogBox.openEditChassis(chassis, title);
      }
   }

   ngOnInit(): void {
   }
}
