import {Injectable} from "@angular/core";
import {Http, Response} from "@angular/http";

import {GlobalsService} from "../shared/index";


@Injectable()
export class AppMainService {

    public allHostUrl = this.gs.getWebContextPath() + '/rest/services/hosts/';

    constructor(public http: Http,
                public gs: GlobalsService) {}

    public getHosts(url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.allHostUrl;
        } else {
            url = 'https://10.101.10.8/ui/solarflare/rest/services/hosts/';
        }

        return this.http.get(url)
            .map((response:Response) =>{
            return response.json();
        });

    }

}
