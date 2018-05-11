import { Injectable } from '@angular/core';
import { Http, Response } from "@angular/http";
import { GlobalsService } from '../shared';
import { Nic } from '../views/nic/nic.model';

@Injectable()
export class NicService {

    private rootUrl = 'https://10.101.10.8';
    private nics: Nic[];
    private urlParts = { hostId: '', nicId: '' };
    private readonly baseUrl = this.globalSvc.getWebContextPath() + '/rest/services/hosts/';


    constructor(private globalSvc: GlobalsService, private http: Http) { }

    setUrlParts(hostId, nicId) {
        this.urlParts = { hostId, nicId };
    }

    setNicDetails(data) {
        if (data) {
            this.buildNics(data);
        }
    }

    private buildNics(data) {
        this.nics = [];
        if (data && data.length > 0) {
            data.forEach(host => {
                if (host.children && host.children.length > 0) {
                    host.children.forEach(adapter => {
                        const adapterNics = this.buildNicByAdapter(adapter, host);
                        this.nics = [...this.nics, ...adapterNics];
                    });
                }
            });
        }
    }

    buildNicByAdapter(adapter, host): Nic[] {
        const nics = [];
        if (adapter.children && adapter.children.length > 0) {
            adapter.children.forEach(nic => {
                const newNic = this.buildNicObject(host, adapter, nic);
                nics.push(newNic);
            });
        }
        return nics;
    }

    private buildNicObject(host, adapter, nic): Nic {
        const nicObj = new Nic();
        nicObj.hostId = host ? host.id : '';
        nicObj.adapterId = adapter ? adapter.id : '';
        nicObj.deviceId = adapter ? adapter.deviceId : '';
        nicObj.subSystemDeviceId = adapter ? adapter.subSystemDeviceId : '';
        nicObj.subSystemVendorId = adapter ? adapter.subSystemVendorId : '';
        nicObj.id = nic.id;
        nicObj.name = nic.name;
        nicObj.deviceName = nic.deviceName;
        nicObj.driverName = nic.driverName;
        nicObj.driverVersion = nic.driverVersion;
        nicObj.interfaceName = nic.interfaceName;
        nicObj.macAddress = nic.macAddress;
        nicObj.pciBusNumber = nic.pciBusNumber;
        nicObj.pciFunction = nic.pciFunction;
        nicObj.pciId = nic.pciId;
        nicObj.portSpeed = nic.portSpeed;
        nicObj.status = nic.status;
        nicObj.type = nic.type;
        nicObj.vendorName = nic.vendorName;
        nicObj.currentMTU = nic.currentMTU;
        nicObj.maxMTU = nic.maxMTU;
        return nicObj;
    }

    getNicFromStorage(hostId, nicId): Nic {
        let nicResult = null;
        if (this.nics && this.nics.length > 0) {
            nicResult = this.nics.find((nic: Nic) => {
                return String(nic.id) === String(nicId) && String(nic.hostId) === String(hostId);
            })
        }
        return nicResult;
    }

    getNicDetails() {
        let url = '';
        if (this.globalSvc.isPluginMode()) {
            url = this.baseUrl + this.urlParts.hostId + '/adapters/' + this.urlParts.nicId + '/nics';
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/' +
                this.urlParts.hostId + '/adapters/' + this.urlParts.nicId + '/nics';
        }
        const headers = this.globalSvc.getCacheControlHeaders()
        return this.http.get(url, headers)
            .map((response: Response) => {
                return response.json();
            });
    }

    getStatDetails() {
        let url = '';
        if (this.globalSvc.isPluginMode()) {
            url = this.baseUrl + this.urlParts.hostId + '/adapters/' + this.urlParts.nicId + '/statistics';
        } else {
            url = this.rootUrl + '/ui/solarflare/rest/services/hosts/' +
                this.urlParts.hostId + '/adapters/' + this.urlParts.nicId + '/statistics';
        }
        const headers = this.globalSvc.getCacheControlHeaders()
        return this.http.get(url, headers)
            .map((response: Response) => {
                return this.nicStatsDataMapper(response.json());
            });

    }

    private nicStatsDataMapper(res) {
        return [
            { name: 'Time Period From', value: res['timePeriod_from'] || '' },
            { name: 'Time Period To', value: res['timePeriod_to'] || '' },
            { name: 'Packets received', value: res['packetsReceived'] || '' },
            { name: 'Packets sent', value: res['packetsSent'] || '' },
            { name: 'Bytes received', value: res['bytesReceived'] || '' },
            { name: 'Bytes sent', value: res['bytesSent'] || '' },
            { name: 'Receive packets dropped', value: res['receivePacketsDropped'] || '' },
            { name: 'Transmit packets dropped', value: res['transmitPacketsDropped'] || '' },
            { name: 'Multicast packets received', value: res['multicastPacketsReceived'] || '' },
            { name: 'Broadcast packets received', value: res['broadcastPacketsReceived'] || '' },
            { name: 'Multicast packets sent', value: res['multicastPacketsSent'] || '' },
            { name: 'Broadcast packets sent', value: res['broadcastPacketsSent'] || '' },
            { name: 'Total receive errors', value: res['totalReceiveError'] || '' },
            { name: 'Total transmit errors', value: res['totalTransmitErrors'] || '' }
        ];
    }
}
