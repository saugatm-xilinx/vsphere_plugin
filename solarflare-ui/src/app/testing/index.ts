import { TestBed } from "@angular/core/testing";

import { GlobalsService } from "../shared/index";
import { webPlatformStub } from "../shared/dev/webPlatformStub";

export * from "./router-stubs";

/**
 * Stub for testing in plugin mode or dev mode
 */
export const globalStub = {
   pluginMode: true,
   webPlatform: webPlatformStub
};

/**
 * Initialization for unit tests
 */
export function initGlobalService(pluginMode: boolean): GlobalsService {

   globalStub.pluginMode = pluginMode;
   return TestBed.get(GlobalsService);
}

export const appErrorHandlerStub = {
   httpPromiseError(error: any): Promise<any> {
      return Promise.reject("error message from appErrorHandlerStub");
   }
};
