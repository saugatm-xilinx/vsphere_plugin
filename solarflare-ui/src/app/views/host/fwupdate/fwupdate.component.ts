import {Component, OnInit, OnDestroy} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Http} from "@angular/http";
import {GlobalsService} from "../../../shared/globals.service";
import {Subscription} from 'rxjs/Subscription';

@Component({
    selector: 'app-fwupdate',
    templateUrl: './fwupdate.component.html',
    styleUrls: ['./fwupdate.component.scss']
})
export class FwupdateComponent implements OnInit {
    subscription: Subscription;
    public hostAdaptersListUrl = this.gs.getWebContextPath() + '/rest/services/hosts/';
    public latestUpdateModal = false;
    public customUpdateModal = false;
    public params = {};
    public adapterList = [];
    public updatable = {
        latest: true,
        custom: true
    };
    public selectedAdapters = [];
    public validateLatestUpdateModal = false;

    constructor(private activatedRoute: ActivatedRoute,
                private http: Http,
                public gs: GlobalsService) {
        this.activatedRoute.parent.params.subscribe((params: Params) => {
            this.params = params;
        });
    }

    getAdapterList() {
        this.adapterList = [];
        let url = "";
        if (this.gs.isPluginMode()) {
            url = this.hostAdaptersListUrl + this.params['id'] + '/adapters/';
        } else {
            url = 'https://10.101.10.7/ui/solarflare/rest/services/hosts/' + this.params['id'] + '/adapters/';
        }
        this.http.get(url)
            .subscribe(
                data => {
                    this.adapterList = data.json();
                },
                err => {
                    console.error(err);
                    this.devMode();
                }
            );
    }

    ngOnInit() {
        this.getAdapterList();
        //this.devMode();
    }

    validateLatestUpdate(remove: string) {
        let updatable = 0, invalid = 0, filterdAdapters= [];
        this.selectedAdapters.forEach((value, index) => {
            if (value.laterVersionAvailable) {
                updatable++;
            } else {
                invalid++;
            }
        });
        if (remove == 'remove') {
            this.selectedAdapters.forEach((value, index) => {
                if (value.laterVersionAvailable) {
                    filterdAdapters.push(value);
                }
            });
            this.selectedAdapters = filterdAdapters;
            this.validateLatestUpdateModal = false;
            if (this.selectedAdapters && this.selectedAdapters.length !== 0) {
                this.latestUpdateModal = true;
                return true;
            }
        }

        updatable !== 0 ? this.updatable.latest = true : this.updatable.latest = false;

        if (invalid == 0 && updatable !== 0) {
            this.latestUpdateModal = true;
            return true;
        } else {
            this.validateLatestUpdateModal = true;
            return false;
        }
    }

    latestUpdate() {
        let url = "";
        if (this.gs.isPluginMode()) {
            url = this.hostAdaptersListUrl + this.params['id'] + '/adapters/latest';
        } else {
            url = 'https://10.101.10.7/ui/solarflare/rest/services/hosts/' + this.params['id'] + '/adapters/latest';
        }
        this.http.post(url, this.selectedAdapters)
            .subscribe(
                data => {
                    this.getAdapterList();
                    this.latestUpdateModal = false;
                },
                err =>
                    console.error(err)
            );

    }

    devMode() {
        this.adapterList =[{
            "name": "SFC9140-00:0f:53:2f:bf:20",
            "type": "ADAPTER",
            "id": "SFC9140",
            "versionController": "6.2.5.1000 rx1 tx1",
            "versionBootROM": "5.0.5.1002",
            "versionUEFIROM": "1.1.1.0",
            "versionFirmware": "1.1.1.0",
            "latestVersion": {
                "controlerVersion": "6.2.7.1000",
                "bootROMVersion": null,
                "uefiVersion": null,
                "firmewareFamilyVersion": null
            },
            "status": [{
                "status": "UPLOADING_FAIL",
                "message": "Fail to update Controller firmware image",
                "timeStamp": 1513925798583,
                "type": "controller"
            }],
            "children": [{
                "type": "NIC",
                "id": "key-vim.host.PhysicalNic-vmnic6",
                "name": "vmnic6",
                "deviceId": "2339",
                "deviceName": "SFC9140",
                "subSystemDeviceId": null,
                "vendorId": "6436",
                "vendorName": "Solarflare",
                "subSystemVendorId": null,
                "driverName": null,
                "driverVersion": null,
                "macAddress": "00:0f:53:2f:bf:20",
                "status": null,
                "interfaceName": null,
                "portSpeed": null,
                "currentMTU": null,
                "maxMTU": null,
                "pciId": "0000:04:00.0",
                "pciFunction": null,
                "pciBusNumber": null
            }],
            "laterVersionAvailable": true
        }, {
            "name": "SFC9220-00:0f:53:4b:66:50",
            "type": "ADAPTER",
            "id": "SFC9220",
            "versionController": "6.2.0.1016 rx1 tx1",
            "versionBootROM": "0.0.0.0",
            "versionUEFIROM": "1.1.1.0",
            "versionFirmware": "1.1.1.0",
            "latestVersion": {
                "controlerVersion": null,
                "bootROMVersion": null,
                "uefiVersion": null,
                "firmewareFamilyVersion": null
            },
            "status": [],
            "children": [{
                "type": "NIC",
                "id": "key-vim.host.PhysicalNic-vmnic4",
                "name": "vmnic4",
                "deviceId": "2563",
                "deviceName": "SFC9220",
                "subSystemDeviceId": null,
                "vendorId": "6436",
                "vendorName": "Solarflare",
                "subSystemVendorId": null,
                "driverName": null,
                "driverVersion": null,
                "macAddress": "00:0f:53:4b:66:50",
                "status": null,
                "interfaceName": null,
                "portSpeed": null,
                "currentMTU": null,
                "maxMTU": null,
                "pciId": "0000:82:00.0",
                "pciFunction": null,
                "pciBusNumber": null
            }],
            "laterVersionAvailable": false
        }, {
            "name": "SFC9220-00:0f:53:4b:66:51",
            "type": "ADAPTER",
            "id": "SFC9220",
            "versionController": "6.2.0.1016 rx1 tx1",
            "versionBootROM": "0.0.0.0",
            "versionUEFIROM": "1.1.1.0",
            "versionFirmware": "1.1.1.0",
            "latestVersion": {
                "controlerVersion": null,
                "bootROMVersion": null,
                "uefiVersion": null,
                "firmewareFamilyVersion": null
            },
            "status": [],
            "children": [{
                "type": "NIC",
                "id": "key-vim.host.PhysicalNic-vmnic5",
                "name": "vmnic5",
                "deviceId": "2563",
                "deviceName": "SFC9220",
                "subSystemDeviceId": null,
                "vendorId": "6436",
                "vendorName": "Solarflare",
                "subSystemVendorId": null,
                "driverName": null,
                "driverVersion": null,
                "macAddress": "00:0f:53:4b:66:51",
                "status": null,
                "interfaceName": null,
                "portSpeed": null,
                "currentMTU": null,
                "maxMTU": null,
                "pciId": "0000:82:00.1",
                "pciFunction": null,
                "pciBusNumber": null
            }],
            "laterVersionAvailable": false
        }, {
            "name": "SFC9140-00:0f:53:2f:bf:21",
            "type": "ADAPTER",
            "id": "SFC9140",
            "versionController": "6.2.5.1000 rx1 tx1",
            "versionBootROM": "5.0.5.1002",
            "versionUEFIROM": "1.1.1.0",
            "versionFirmware": "1.1.1.0",
            "latestVersion": {
                "controlerVersion": "6.2.7.1000",
                "bootROMVersion": null,
                "uefiVersion": null,
                "firmewareFamilyVersion": null
            },
            "status": [{
                "status": "UPLOADING_FAIL",
                "message": "Fail to update Controller firmware image",
                "timeStamp": 1513925798583,
                "type": "controller"
            }],
            "children": [{
                "type": "NIC",
                "id": "key-vim.host.PhysicalNic-vmnic7",
                "name": "vmnic7",
                "deviceId": "2339",
                "deviceName": "SFC9140",
                "subSystemDeviceId": null,
                "vendorId": "6436",
                "vendorName": "Solarflare",
                "subSystemVendorId": null,
                "driverName": null,
                "driverVersion": null,
                "macAddress": "00:0f:53:2f:bf:21",
                "status": null,
                "interfaceName": null,
                "portSpeed": null,
                "currentMTU": null,
                "maxMTU": null,
                "pciId": "0000:04:00.1",
                "pciFunction": null,
                "pciBusNumber": null
            }],
            "laterVersionAvailable": true
        }];
    }



}



