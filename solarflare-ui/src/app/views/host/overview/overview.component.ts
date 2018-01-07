import {Component, OnInit, OnDestroy} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Http} from "@angular/http";
import {GlobalsService} from "../../../shared/globals.service";
import {Subscription} from 'rxjs/Subscription';
import {HostsService} from "../../../services/hosts.service";

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})

export class OverviewComponent implements OnInit {
    subscription: Subscription;
    public params = {};
    public hostDetail = {};

    constructor(private activatedRoute: ActivatedRoute,
                private http: Http,
                public gs: GlobalsService,
                private hs: HostsService) {
        /*      this.activatedRoute.params.subscribe( (params : Params) => {
                  this.params = params;
                  console.log(this.params);

              });*/
        this.activatedRoute.parent.params.subscribe((params: Params) => {
            this.params = params;
        });
    }

    ngOnInit() {
        this.getHostDetail();
    }

    getHostDetail(){
        this.hs.getHostDetails(this.params['id'])
            .subscribe(
                data => {
                    this.hostDetail = data
                },
                err => {
                    console.error(err);
                    //this.devMode();
                }
            );
    }

    devMode() {
        this.hostDetail = {
            "type": "HOST",
            "id": "host-9",
            "name": "10.101.10.3",
            "children": [],
            "adapterCount": "2",
            "portCount": "1",
            "driverVersion": "444",
            "cimProviderVersion": "2222"
        };
    }

}

