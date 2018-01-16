import { ErrorHandler, Injectable, Inject, forwardRef } from "@angular/core";
import { Observable } from "rxjs/Rx";
import { Response } from "@angular/http";

import { GlobalsService }     from "../shared/index";

export const liveDataHelp =
      " => Check that plugin is also deployed in vSphere Client. Go to Settings page to set client id for live data.";
export const jsonServerHelp =
      "No mock data available :-( Start json-server with command: npm run json-server";

/**
 * Centralized error handling.
 * - override Angular's default ErrorHandler
 * - provide a common API for reporting http errors in Promise or Observable form
 */
@Injectable()
export class AppErrorHandler implements ErrorHandler {

   // Use forwardRef because of circular dependencies with injection of GlobalsService
   constructor(@Inject(forwardRef(() => GlobalsService)) private gs: GlobalsService) {
   }

   /**
    * handleError is called by default when no error handling code is present.
    * @param error
    */
   handleError(error) {
      console.error(error);
      // Add more formatting as necessary
   }

   /**
    * Handle error message for http promises
    * @param error
    * @returns {Promise<never>}
    */
   httpPromiseError(error: any): Promise<any> {
      return Promise.reject(
            this.formatHttpError(error, this.gs.isPluginMode(), this.gs.useLiveData()));
   }

   /**
    * Handle error message for http observables
    * @param error
    * @returns {Observable<never>}
    */
   httpObservableError(error: any): Observable<string> {
      return Observable.throw(
            this.formatHttpError(error, this.gs.isPluginMode(), this.gs.useLiveData()));
   }

   /**
    * @param error
    * @returns a formatted error message
    */
   getHttpError(error: any): string {
      return this.formatHttpError(error, this.gs.isPluginMode(), this.gs.useLiveData());
   }

   private formatHttpError(error: any, isPluginMode = true, useLiveData = true) {
      let errMsg;
      if (typeof error === 'string') {
         errMsg = error;

      } else if (error.message) {
         errMsg = error.message;

      } else if (error instanceof Response && error.status === 504 && !isPluginMode) {
         // Gateway error must be because live data is not setup yet
         errMsg = error.text() + liveDataHelp;

      } else if (error instanceof Response && error.status === 500) {
         // Server returned an error, either in json or text format
         errMsg = error.statusText + ": ";
         try {
            const errorBody = error.json();
            // Remove extra lines (stack trace), they are available in the browser console or server log
            errMsg += errorBody.message.replace(/\n.*/g, "");
         } catch (e) {
            errMsg += this.getErrorText(error)
            if (!isPluginMode && useLiveData) {
               errMsg += liveDataHelp;
            }
         }

      } else if (error.status) {
         errMsg = "Http error: " + error.status + (error.statusText ? ", " + error.statusText : "");
         if (error.status === 401 && !isPluginMode) {
            // add help for not-authorized status
            errMsg += liveDataHelp;
         } else if (error.status === 404 && error.url) {
            errMsg += " at URL: " + error.url;
         }

      } else if (error.status === 0 && !useLiveData) {
         // dev mode help for people who forgot to start the json-server
         errMsg = jsonServerHelp;

      } else {
         errMsg = "An unknown error occurred!";
      }
      return errMsg;
   }

   private getErrorText(error: Response): string {
      const text: string = error.text();
      if (text.indexOf("<h1>") > 0) {
         return text.substring((text.indexOf("<h1>") + 4), text.indexOf("</h1>"));
      } else {
         return text.replace(/\n.*/g, "");
      }
   }
}
