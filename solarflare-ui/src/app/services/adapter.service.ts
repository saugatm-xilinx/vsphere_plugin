import { Injectable } from '@angular/core';
import { GlobalsService } from "../shared/globals.service";
import { Http, Response } from "@angular/http";

@Injectable()
export class AdapterService {

    private rootUrl = 'https://10.101.10.7';
    public hostDetailUrl = this.gs.getWebContextPath() + '/rest/services/hosts/';

    constructor(private gs: GlobalsService,
        private http: Http) { }

    public getAdapterDetails(id, url?: string) {
        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + id.hostid + '/adapters/' + id.adapterid + '/overview';
        } else {
            url = this.rootUrl + `/ui/solarflare/rest/services/hosts/${id.hostid}/adapters/${id.adapterid}/overview`;
        }

        return this.http.get(url)
            .map((response: Response) => {
                return response.json();
            });
    }
}
