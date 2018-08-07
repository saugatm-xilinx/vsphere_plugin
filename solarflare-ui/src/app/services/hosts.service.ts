import { Injectable } from '@angular/core';
import { GlobalsService } from "../shared/globals.service";
import { Http, Response } from "@angular/http";



@Injectable()
export class HostsService {

    public hostDetailUrl = this.gs.getWebContextPath() + '/rest/services/hosts/';
    private rootUrl = 'https://10.101.10.8';
    private hosts = [];
    private adapters = [];
    private configs = null;

    constructor(private gs: GlobalsService, private http: Http) { }

    public getHostDetails(hostId: string, url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/';
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/' +
                hostId + '/';
        }
        const headers = this.gs.getCacheControlHeaders()
        return this.http.get(url, headers)
            .map((response: Response) => {
                return response.json();
            });

    }

    public getAdapters(hostId: string, url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/adapters/';
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/' +
                hostId + '/adapters/';
        }
        const headers = this.gs.getCacheControlHeaders()
        return this.http.get(url, headers)
            .map((response: Response) => {
                return response.json();
            });

    }

    public onSubmitFile(hostId: string, payload: object, url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/adapters/updateCustomWithBinary';
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/' +
                hostId + '/adapters/updateCustomWithBinary';
        }

        return this.http.post(url, payload)
            .map((response: Response) => {
                return response.json();
            });
    }

    public onSubmitUrl(hostId: string, payload: object, url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/adapters/updateCustomWithUrl';
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/' +
                hostId + '/adapters/updateCustomWithUrl';
        }

        return this.http.post(url, payload)
            .map((response: Response) => {
                return response.json();
            });
    }

    public latestUpdate(hostId: string, adapters: object, url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/adapters/latest';
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/' +
                hostId + '/adapters/latest';
        }

        return this.http.post(url, adapters)
            .map((response: Response) => {
                try {
                    return response.json();
                } catch (e) {
                    return response;
                }
            });
    }

    public getStatus(taskId: string) {

        let url: string;
        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + 'tasks/' + taskId;
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/tasks/' + taskId;
        }
        const headers = this.gs.getCacheControlHeaders()
        return this.http.get(url, headers)
            .map((response: Response) => {
                return response.json();
            }).catch(error => {
                return error;
            });
    }

    public getConfiguration(hostId: string, url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/configuration/';
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/' +
                hostId + '/configuration/';
        }
        const headers = this.gs.getCacheControlHeaders()
        return this.http.get(url, headers)
            .map((response: Response) => {
                return response.json();
            });

    }

    public putConfiguration(hostId: string, payload: object, url?: string) {

        if (this.gs.isPluginMode()) {
            url = this.hostDetailUrl + hostId + '/configuration/';
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/' +
                hostId + '/configuration/';
        }

        return this.http.post(url, payload)
            .map((response: Response) => {
                return response.json();
            });

    }

    setHosts(hosts) {
        this.hosts = hosts;
    }

    updateHostDetail(hostDetail) {
        let isPresent = false;
        if (hostDetail && hostDetail.id) {
            this.hosts.map((host, index) => {
                if (host.id === hostDetail.id) {
                    this.hosts[index] = hostDetail;
                    isPresent = true;
                }
            })
        }
        if (!isPresent) {
            this.hosts.push(hostDetail);
        }
    }

    getHost(hostId) {
        const hostResult = this.hosts.filter(host => {
            return String(host.id) === String(hostId);
        })
        return hostResult.length === 1 ? hostResult[0] : {};
    }

    setAdapter(adapterDetail) {
        let isPresent = false;
        this.adapters.map((adapter, index) => {
            if (String(adapter.hostId) === String(adapterDetail.hostId)) {
                this.adapters[index] = adapterDetail;
                isPresent = true;
            }
        })
        if (!isPresent) {
            this.adapters.push(adapterDetail);
        }
    }

    getAdapter(hostId): any {
        const adapterResult = this.adapters.filter(adapter => {
            return String(adapter.hostId) === String(hostId);
        })
        return adapterResult.length === 1 ? adapterResult[0] : { hostId: '', adapters: [], isLatest: false };
    }

    getAdapterNameByHostIdAndNicId(hostId, nicId) {
        let adapterName = '';
        const adapterResult = this.hosts.forEach(host => {
            if (String(host.id) === String(hostId)) {
                host.children.forEach(adapter => {
                    adapter.children.forEach(nic => {
                        if (String(nic.id) === String(nicId)) {
                            adapterName = adapter.name;
                        }
                    });
                });
            }
        })
        return adapterName;
    }

    getAdapterOverviewDetail(hostId, nicId) {
        const overviewDetail = { adapterName: '', partNumber: '', serialNumber: '' }
        const adapterResult = this.hosts.forEach(host => {
            if (String(host.id) === String(hostId)) {
                host.children.forEach(adapter => {
                    adapter.children.forEach(nic => {
                        if (String(nic.id) === String(nicId)) {
                            overviewDetail.adapterName = adapter.name;
                            overviewDetail.partNumber = adapter.partNumber;
                            overviewDetail.serialNumber = adapter.serialNumber;
                        }
                    });
                });
            }
        })
        return overviewDetail;
    }

    updateAdapterOverview(detail, hostId, nicId) {
        const adapterResult = this.hosts.forEach(host => {
            if (String(host.id) === String(hostId)) {
                host.children.forEach(adapter => {
                    adapter.children.forEach(nic => {
                        if (String(nic.id) === String(nicId)) {
                            adapter.partNumber = detail.partNumber;
                            adapter.serialNumber = detail.serialNumber
                        }
                    });
                });
            }
        })
    }

    setConfigs(configs) {
        this.configs = configs;
    }

    getConfigs() {
        return this.configs;
    }
}
