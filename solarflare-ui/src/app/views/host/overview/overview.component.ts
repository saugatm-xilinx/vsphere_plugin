import {Component, OnInit, OnDestroy} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Http} from "@angular/http";
import {GlobalsService} from "../../../shared/globals.service";
import {Subscription} from 'rxjs/Subscription';

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})

export class OverviewComponent implements OnInit {
    subscription: Subscription;
    public hostDetailUrl = this.gs.getWebContextPath() + '/rest/services/hosts/';
    public params = {};
    public hostDetail = {};

    constructor(private activatedRoute: ActivatedRoute,
                private http: Http,
                public gs: GlobalsService) {
        /*      this.activatedRoute.params.subscribe( (params : Params) => {
                  this.params = params;
                  console.log(this.params);

              });*/
        this.activatedRoute.parent.params.subscribe((params: Params) => {
            this.params = params;
        });
    }

    ngOnInit() {
        let url = "";
        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + this.params['id'] + '/';
        } else {
            url = 'https://10.101.10.7/ui/solarflare/rest/services/hosts/' + this.params['id'] + '/';
        }

        this.http.get(url)
            .subscribe(
                data => {
                    this.hostDetail = data.json()
                },
                err => console.error(err)
            );
    }



}

/*
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

 */