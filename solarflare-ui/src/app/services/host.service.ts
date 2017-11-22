import { Injectable } from "@angular/core";
import { Http } from "@angular/http";
import { Observable } from "rxjs/Observable";

import { GlobalsService, AppAlertService } from "../shared/index";
import { AppErrorHandler } from "../shared/appErrorHandler";
import { APP_CONFIG } from "../shared/app-config";
import { Host, HostError } from "./host.model";

// Object type
export const hostType = "HostSystem";

/**
 * The result of getHosts(), containing either an array of hosts or an error
 */
export class HostList {
   constructor(public hosts: Host[], public error: string = null) {
   }
}

@Injectable()
export class HostService {
   constructor(private http: Http,
               private errorHandler: AppErrorHandler,
               private gs: GlobalsService,
               private appAlertService: AppAlertService) {
   }

   /**
    * Build the REST url endpoint for retrieving a list of properties.
    * This is mapped to the DataAccessController on the java side.
    */
   buildDataUrl (objectId, propList): string {
      const propStr = propList.toString();
      const dataUrl = this.gs.getWebContextPath() +
            "/rest/data/properties/" + objectId + "?properties=" + propStr;
      return dataUrl;
   };

   private getHostsUrl(): string {
      let url: string;
      if (this.gs.useLiveData()) {
         // Use plugin's REST endpoint to get list of object names with type HostSystem
         url = this.gs.getWebContextPath() + "/rest/data/list/?"
               + "targetType=" + hostType + "&properties=name";
      } else {
         url = APP_CONFIG.getMockDataUrl(this.gs.isPluginMode()) + "/hosts";
      }
      return url;
   }

   private getHostPropertiesUrl(objectId: string, properties: string[]): string {
      let url: string;
      if (this.gs.useLiveData()) {
         // Use rest/data/properties/[objectId]?properties=... to get data from the Java service
         url = this.buildDataUrl(objectId, properties);
      } else {
         url = APP_CONFIG.getMockDataUrl(this.gs.isPluginMode()) + "/hosts/" + objectId;
      }
      return url;
   }

   /**
    * Get all hosts with some default properties.
    *
    * @returns an Observable with a Host array or an error message
    */
   getHosts(): Observable<HostList> {
      const headers = this.gs.getHttpHeaders();
      const useLiveData = this.gs.useLiveData();

      return this.http.get(this.getHostsUrl(), headers)
            // Normal response has a data field, mock response from db.json doesn't
            .map(response => {
               const hosts = (useLiveData ? response.json().data : response.json()) as Host[];
               return new HostList(hosts);
            })
            .catch(error =>
                  Observable.of(new HostList([], this.errorHandler.getHttpError(error))));
   }

   /**
    * Query a list of properties for a specific host id.
    *
    * @param objectId
    * @param properties  Array of property names matching the Host model definition and the db.json mock data
    * @returns an Observable with host data or a HostError
    */
   getHostProperties(objectId: string, properties: string[]): Observable<Host | HostError> {
      const headers = this.gs.getHttpHeaders();
      const url = this.getHostPropertiesUrl(objectId, properties);
      const useLiveData = this.gs.useLiveData();

      return this.http.get(url, headers)
            .map(response => Host.convertProperties(response.json(), useLiveData))
            .catch(error => {
               const errorMsg = this.errorHandler.getHttpError(error);
               return Observable.of(new HostError(objectId, errorMsg));
            });
   }

   /**
    * Shortcut to retrieve a host name and handle errors
    * @param objectId
    * @param callback  Function to be called with the new Host object, which has only id and name.
    */
   getHostName(objectId: string, callback: Function): void {
      this.getHostProperties(objectId, ["name"])
            .toPromise()
            .then(host => callback(host))
            .catch(errorMsg => {
               this.appAlertService.showError(errorMsg);
            });
   }
}
