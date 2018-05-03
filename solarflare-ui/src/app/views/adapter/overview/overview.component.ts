import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params} from '@angular/router';
import {AdapterService} from "../../../services/adapter.service";
import {GlobalsService} from "../../../shared/globals.service";

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit, OnDestroy {
    public params = {};
    public adapterDetail = {};
    public getOverviewErr = false;

  constructor(private activatedRoute: ActivatedRoute,
              public gs: GlobalsService,
              private as: AdapterService) {

      this.activatedRoute.parent.params.subscribe((params: Params) => {
          this.params = params;
          this.adapterDetails();
      });
  }

  ngOnInit() {}

  ngOnDestroy() {}

  adapterDetails() {
      this.getOverviewErr = false;
      this.as.getAdapterDetails(this.params)
          .subscribe(
              data => {
                  this.adapterDetail = data
              },
              err => {
                  console.error(err);
                  if(err.status == 401){
                    // window.location.reload()
                  }
                  this.getOverviewErr = true;
              }
          );
  }




}
