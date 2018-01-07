import { Injectable } from '@angular/core';
import {GlobalsService} from "../shared/globals.service";
import {Http, Headers, Response} from "@angular/http";
import {Observable} from "rxjs/Observable";


@Injectable()
export class HostsService {
    public hostDetailUrl = this.gs.getWebContextPath() + '/rest/services/hosts/';

  constructor(private gs: GlobalsService,
              private http: Http) { }

  public getHostDetails(hostId:string, url?:string){

      if (this.gs.isPluginMode()) {
          url = this.hostDetailUrl + hostId + '/';
      } else {
          url = 'https://10.101.10.8/ui/solarflare/rest/services/hosts/' +
              hostId + '/';
      }

      return this.http.get(url)
          .map((response:Response) =>{
          return response.json();
      });

  }

    public getAdapters(hostId: string, url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/adapters/';
        } else {
            url = 'https://10.101.10.8/ui/solarflare/rest/services/hosts/' +
                hostId + '/adapters/';
        }

        return this.http.get(url)
            .map((response:Response) =>{
            return response.json();
        });

    }

    public onSubmitFile(hostId: string, payload: object, url?:string){

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/adapters/updateCustomWithBinary';
        } else {
            url = 'https://10.101.10.8/ui/solarflare/rest/services/hosts/' +
                hostId + '/adapters/updateCustomWithBinary';
        }

        return this.http.post(url, payload)
            .map((response:Response) =>{
            return response.json();
        });
    }

    public onSubmitUrl(hostId: string, payload: object, url?:string){

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/adapters/updateCustomWithUrl';
        } else {
            url = 'https://10.101.10.8/ui/solarflare/rest/services/hosts/' +
                hostId + '/adapters/updateCustomWithUrl';
        }

        return this.http.post(url, payload)
            .map((response:Response) =>{
            return response.json();
        });
    }

    public latestUpdate(hostId: string, adapters: object, url?:string) {

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/adapters/latest';
        } else {
            url = 'https://10.101.10.8/ui/solarflare/rest/services/hosts/' +
                hostId + '/adapters/latest';
        }

        return this.http.post(url, adapters)
            .map((response:Response) =>{
            try{
                return response.json();
            }catch (e){
                return response;
            }
        });
    }

    public getStatus(taskId: string){

        let url: string;
        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + 'tasks/' + taskId;
        } else {
            url = 'https://10.101.10.8/ui/solarflare/rest/services/hosts/tasks/' + taskId ;
        }

        return this.http.get(url)
            .map((response:Response) =>{
                return response.json();
            }).catch(error => {
                return error;
            });
    }

}
