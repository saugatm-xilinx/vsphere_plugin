import {Component, OnInit} from '@angular/core';
import { ActivatedRoute, Params} from '@angular/router';
import {GlobalsService} from "../../../shared/globals.service";
import {HostsService} from "../../../services/hosts.service";

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})

export class OverviewComponent implements OnInit {
    public params = {};
    public hostDetail = {};
    public getOverviewErr = false;

    constructor(private activatedRoute: ActivatedRoute,
                public gs: GlobalsService,
                private hs: HostsService) {

        this.activatedRoute.parent.params.subscribe((params: Params) => {
            this.params = params;
        });
    }

    ngOnInit() {
        this.getHostDetail();
    }

    getHostDetail() {
        this.getOverviewErr = false;
        this.hs.getHostDetails(this.params['id'])
            .subscribe(
                data => {
                    this.hostDetail = data
                },
                err => {
                    console.error(err);
                    this.getOverviewErr = true;
                }
            );
    }

}

