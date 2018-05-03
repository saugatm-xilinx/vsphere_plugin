import { Component, OnInit, Input } from '@angular/core';
import { NavService } from "../../services/nav.service";
import { GlobalsService } from "../globals.service";
import { Observable } from "rxjs/Observable";
import { ActivatedRoute } from "@angular/router";

/**
 * Subnav component with tabs to switch between object views
 */
@Component({
   selector: 'subnav',
   templateUrl: './subnav.component.html',
   styleUrls: ['./subnav.component.scss']
})
export class SubnavComponent implements OnInit {
   objectId$: Observable<string>;

   // objectType injected by the parent component
   @Input() objectType;

   constructor(private gs: GlobalsService,
               public nav: NavService,
               private route: ActivatedRoute) {
      this.objectId$ = this.route.paramMap
            .map(paramMap => paramMap.get('id'));
   }

   ngOnInit() {
   }

}
