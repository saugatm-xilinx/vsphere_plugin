import {Injectable} from "@angular/core";
import {Http, Headers, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";

import {GlobalsService, AppAlertService} from "../shared/index";
import {AppErrorHandler} from "../shared/appErrorHandler";
import {APP_CONFIG} from "../shared/app-config";
import {toPromise} from "rxjs/operator/toPromise";


@Injectable()
export class AppMainService {
    public current_user = null;
    public headers = null;
    public hostList = null;

    constructor(public http: Http,
                public gs: GlobalsService) {
    }

    getHeader() {
        //this.current_user = JSON.parse(sessionStorage.getItem('currentUser'));
        this.headers = new Headers();
        this.headers.set('content-type', 'application/json');
        this.headers.set('Accept', 'application/json');
        //this.headers.append('Authorization', 'Basic bXN5c0B2c3BoZXJlLmxvY2FsOk1zeXNAMTIz');
        return this.headers;
    }

    get(url) {
        return this.http.get(url, {
            headers: this.getHeader(),
            withCredentials : true
    });
    }

    public getHosts() {
        let url = this.gs.getWebContextPath() + '/rest/services/hosts';

        this.http.get('https://10.101.10.7/ui/solarflare/rest/services/hosts/')
            .subscribe(
                data => { this.hostList = data.json();
                    this.hostList = Array.of(this.hostList);
                    return this.hostList;
                },
                err => console.error(err)
            );
    }

}
