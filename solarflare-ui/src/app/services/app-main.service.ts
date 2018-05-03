import { Injectable } from "@angular/core";
import { Http, Response } from "@angular/http";
import { GlobalsService } from "../shared/index";


@Injectable()
export class AppMainService {

    private rootUrl = 'https://10.101.10.8';
    public allHostUrl = this.gs.getWebContextPath() + '/rest/services/hosts/';

    constructor(public http: Http,
        public gs: GlobalsService) { }

    public getHosts(url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.allHostUrl;
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/';
        }
        const headers = this.gs.getCacheControlHeaders()
        return this.http.get(url, headers)
            .map((response: Response) => {
                return response.json();
            });

    }

}
