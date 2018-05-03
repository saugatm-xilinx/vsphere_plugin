// External imports
import { Response, ResponseOptions } from "@angular/http";

// Internal imports
import { AppErrorHandler, liveDataHelp, jsonServerHelp } from "../shared/appErrorHandler";
import { GlobalsService } from "./globals.service";

// Simple service unit tests without assistance from Angular testing utilities

describe("AppErrorHandler tests", () => {
   let appErrorHandler: AppErrorHandler;
   const errorFromServer = "some error message\nsome more lines";
   const errorToDisplay = "some error message";
   let pluginMode: boolean;
   let useLiveData: boolean;

   const globalsServiceStub = {
      isPluginMode: function() {
         return pluginMode;
      },
      useLiveData: function() {
         return useLiveData;
      }
   };

   beforeEach(() => {
      appErrorHandler = new AppErrorHandler(<GlobalsService>globalsServiceStub);
      pluginMode = true;
      useLiveData = true;
   });

   it ("formats server errors correctly - with body object", ()  => {
      const resOptions: ResponseOptions = {
         status: 500,
         body: { message: errorFromServer },
         headers: null,
         url: "some url",
         merge: null
      };
      const error = new Response(resOptions);
      const statusText = "Server error";
      error.statusText = statusText;

      const errMsg = appErrorHandler.getHttpError(error);
      expect(errMsg).toBe(statusText + ": " + errorToDisplay);
   });

   it ("formats server errors correctly - with body string", ()  => {
      const resOptions: ResponseOptions = {
         status: 500,
         body: errorFromServer,
         headers: null,
         url: "some url",
         merge: null
      };
      const error = new Response(resOptions);
      const statusText = "Server error";
      error.statusText = statusText;

      const errMsg = appErrorHandler.getHttpError(error);
      expect(errMsg).toBe(statusText + ": " + errorToDisplay);
   });


   it ("formats http errors correctly for live data", ()  => {
      const resOptions: ResponseOptions = {
         status: 401,
         body: "",
         headers: null,
         url: "some url",
         merge: null
      };
      const error = new Response(resOptions);
      const statusText = "some status";
      error.statusText = statusText;

      let errMsg = appErrorHandler.getHttpError(error);
      expect(errMsg).toBe("Http error: 401, " + statusText);

      pluginMode = false;
      errMsg = appErrorHandler.getHttpError(error);
      expect(errMsg).toBe("Http error: 401, " + statusText + liveDataHelp);

      pluginMode = true;
      error.status = 404;
      errMsg = appErrorHandler.getHttpError(error);
      expect(errMsg).toBe("Http error: 404, " + statusText + " at URL: " + resOptions.url);
   });

   it ("returns json-server help in dev mode", ()  => {
      const resOptions: ResponseOptions = {
         status: 0,
         body: "",
         headers: null,
         url: "some url",
         merge: null
      };
      const error = new Response(resOptions);

      pluginMode = false;
      useLiveData = false;
      const errMsg = appErrorHandler.getHttpError(error);
      expect(errMsg).toBe(jsonServerHelp);
   });
});
