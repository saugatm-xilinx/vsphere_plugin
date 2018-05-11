import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { AdapterService } from "../../../services/adapter.service";
import { GlobalsService } from "../../../shared/globals.service";
import { HostsService } from '../../../services/hosts.service';
import { environment } from "environments/environment"

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit, OnDestroy {
    public isProd = environment.production
    public params = {};
    public adapterDetail = { adapterName: '', portNumber: '', serialNumber: '' };
    adapterName = '';
    public getOverviewErr = false;
    public refreshButtonDisabled

    constructor(private activatedRoute: ActivatedRoute,
        public gs: GlobalsService,
        private as: AdapterService,
        private hostService: HostsService) {

        this.activatedRoute.parent.params.subscribe((params: Params) => {
            this.params = params;
            this.adapterDetail = this.hostService.getAdapterOverviewDetail(this.params['hostid'], this.params['nicId']);
            this.adapterDetails();
        });
    }

    ngOnInit() {
        this.adapterDetail = this.hostService.getAdapterOverviewDetail(this.params['hostid'], this.params['nicId']);
        this.adapterDetails();
    }

    ngOnDestroy() { }

    adapterDetails() {
        this.refreshButtonDisabled = true
        this.getOverviewErr = false;
        this.as.getAdapterDetails(this.params)
            .subscribe(
                data => {
                    this.refreshButtonDisabled = false
                    this.hostService.updateAdapterOverview(data, this.params['hostid'], this.params['nicId']);
                    this.adapterDetail = this.hostService.getAdapterOverviewDetail(this.params['hostid'], this.params['nicId']);
                },
                err => {
                    this.refreshButtonDisabled = false
                    console.error(err);
                    this.getOverviewErr = true;
                }
            );
    }

    refresh() {
        this.adapterDetail = { adapterName: '', portNumber: '', serialNumber: '' }
        this.adapterDetails()
    }


}
