import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { GlobalsService } from "../../../shared/globals.service";
import { HostsService } from "../../../services/hosts.service";
import { environment } from "environments/environment"

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})

export class OverviewComponent implements OnInit {
    public isProd = environment.production
    public params = {};
    public hostDetail = {};
    public getOverviewErr = false;
    public refreshButtonDisable;

    constructor(private activatedRoute: ActivatedRoute,
        public gs: GlobalsService,
        private hs: HostsService,
    ) {

        this.activatedRoute.parent.params.subscribe((params: Params) => {
            this.params = params;
        });
    }

    ngOnInit() {
        this.hostDetail = this.hs.getHost(this.params['id']);
        this.getHostDetail();
    }

    getHostDetail() {
        this.refreshButtonDisable = true
        this.getOverviewErr = false;
        this.hs.getHostDetails(this.params['id'])
            .subscribe(
                data => {
                    this.refreshButtonDisable = false
                    this.hostDetail = data
                    this.hs.updateHostDetail(data);
                },
                err => {
                    console.error(err);
                    this.refreshButtonDisable = false
                    this.getOverviewErr = true;
                }
            );
    }

}

