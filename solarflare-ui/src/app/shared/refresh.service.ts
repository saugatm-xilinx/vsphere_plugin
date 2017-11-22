import { Injectable } from "@angular/core";

import { AppAlertService }   from "./app-alert.service";
import { BehaviorSubject } from "rxjs/BehaviorSubject";

/**
 * Service used to send a "refresh event" to any observer view
 */
@Injectable()
export class RefreshService {
   // Use an rxjs BehaviorSubject to multicast a refresh event to all observers
   // but also to emit a value right away so that views can subscribe to it for
   // their initial rendering (see monitor.component.ts)
   //
   // See http://reactivex.io/rxjs/manual/overview.html#behaviorsubject
   private refreshSource = new BehaviorSubject(true);
   public refreshObservable$ = this.refreshSource.asObservable();

   constructor(private appAlertService: AppAlertService) {
   }

   public refreshView(): void {
      // Close any open alert box here before a view is refreshed
      this.appAlertService.closeAlert();

      // Propagate refresh event to subscribers
      this.refreshSource.next(true);
   }
}
