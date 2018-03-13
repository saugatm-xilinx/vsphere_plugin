import { Injectable } from "@angular/core";
import { Http, Response } from "@angular/http";

import { GlobalsService } from "../shared/index";
import { NicService } from "./nic.service";



@Injectable()
export class AppMainService {

    private rootUrl = 'https://10.101.10.7';
    public allHostUrl = this.gs.getWebContextPath() + '/rest/services/hosts/';

    constructor(public http: Http, private nicService: NicService,
        public gs: GlobalsService) { }

    public getHosts(url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.allHostUrl;
        } else {
            // TODO :- review comments - URLs are being used in many files. We can put all urls
            // in a single file and can access in multiple files.
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/';
        }

        return this.http.get(url)
            .map((response: Response) => {
                this.nicService.setNicDetails(response);
                return response.json();
            });

    }

}
