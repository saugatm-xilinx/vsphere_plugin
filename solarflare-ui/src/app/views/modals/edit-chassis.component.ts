import { Component, Input, Output, OnInit, Injector } from "@angular/core";
import { Router, ActivatedRoute, Params } from "@angular/router";

import { GlobalsService }   from "../../shared/index";
import { Chassis, ChassisService }  from "../../services/chassis/index";
import { APP_CONFIG } from "../../shared/app-config";
import { RefreshService } from "../../shared/refresh.service";
import { AppAlertService } from "../../shared/app-alert.service";

@Component({
   selector: "edit-chassis",
   styleUrls: ["./plugin-modal.scss"],
   templateUrl: "./edit-chassis.component.html"
})
export class EditChassisComponent implements OnInit {
   // Initial chassis object, or null when adding a new chassis
   @Input() currentChassis: Chassis;
   // Chassis being edited
   @Input() chassis: Chassis;

   actionUid: string;
   public pluginFormClass: string;

   constructor(public  gs: GlobalsService,
               private injector: Injector,
               private chassisService: ChassisService,
               private refreshService: RefreshService,
               private route: ActivatedRoute,
               private appAlertService: AppAlertService) {

      // in dev mode the chassis data is passed down from the dialog container
      if (!gs.isPluginMode()) {
         const context = this.injector.get('context');
         if (context) {
            this.currentChassis = new Chassis(context.id, context.name, context.dimensions, context.serverType);
         }
         this.chassis = this.currentChassis ? this.currentChassis.clone() : Chassis.create();
      }

      // css adjustment
      this.pluginFormClass = this.gs.isPluginMode() ? "plugin-modal-form" : "";
   }

   onCancel(): void {
      this.gs.getWebPlatform().closeDialog();
   }

   /**
    * handleSubmit must be defined by all components injected in DialogBox
    */
   public handleSubmit(): void {
      if (this.currentChassis && this.chassis.equals(this.currentChassis)) {
         // Nothing to save
         return;
      }
      const infoMsg = "Chassis " + this.chassis.name + (this.currentChassis ? " was updated" : " was created");
      this.chassisService.save(this.chassis)
            .then(() => {
               // This part is only useful in dev mode because in plugin mode the edit chassis modal dialog
               // cannot communicate with other plugin views.
               this.refreshService.refreshView();
               this.appAlertService.showInfo(infoMsg);
            })
            .catch(errorMsg => {
               this.appAlertService.showError(errorMsg);
            });
   }

   onSubmit(): void {
      this.gs.getWebPlatform().closeDialog();
      this.handleSubmit();
   }


   ngOnInit(): void {
      if (this.gs.isPluginMode()) {
         // Get chassis id and actionUid from query parameters:
         // Initialize the component"s chassis based on the id parameter
         this.route.params.forEach((params: Params) => {
            const chassisId = params["id"];
            this.actionUid = params["actionUid"];

            if (chassisId === "undefined") {
               // Case of chassis creation
               this.chassis = Chassis.create();
            } else {
               this.chassisService.getChassis(chassisId)
                     .toPromise()
                     .then((chassis: Chassis) => {
                        this.chassis = chassis;
                        this.currentChassis = this.chassis.clone();
                     });
            }
         });
      } else {
         // dev mode: chassis editor is opened from the summary view or main view,
         // chassis object is already known and we set actionUid to the value in plugin.xml
         this.actionUid = APP_CONFIG.packageName +
               (this.currentChassis ? ".editChassis" : ".createChassis");
      }
   }

}
