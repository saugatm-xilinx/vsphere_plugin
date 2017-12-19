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
                    this.adapterList = data.json()
                },
                err => {
                    console.error(err);
                    this.devMode();
                }
            );
    }

    ngOnInit() {
        this.getAdapterList();
    }

    validateLatestUpdate(remove: string) {
        let updatable = 0, invalid = 0;
        this.selectedAdapters.forEach((value, index) => {
            if (value.laterVersionAvailable) {
                updatable++;
            } else {
                invalid++;
            }
        });
        if (remove == 'remove') {
            this.selectedAdapters.forEach((value, index) => {
                if (!value.laterVersionAvailable)
                    this.selectedAdapters.splice(index, 1);
            });
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
            url = this.hostAdaptersListUrl + 'adapters/updateToLatest?hostId=' + this.params['id'];
        } else {
            url = 'https://10.101.10.7/ui/solarflare/rest/services/hosts/adapters/updateToLatest?hostId=' + this.params['id'];
        }
        this.http.post(url, this.selectedAdapters)
            .subscribe(
                data => {
                    this.getAdapterList();
                },
                err =>
                    console.error(err)
            );

    }

    devMode() {
        this.adapterList = [{
            "name": "Solarflare SFC9220",
            "type": "ADAPTER",
            "id": "SFC9220",
            "versionController": "6.2.0.1016 rx1 tx1",
            "versionBootROM": "0.0.0.0",
            "versionUEFIROM": "1.1.1.0",
            "versionFirmware": "1.1.1.0",
            "fileData": null,
            "status": {"status": "UPLOADING", "message": "Controller", "timeStamp": ""},
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
                "macAddress": null,
                "status": null,
                "interfaceName": null,
                "portSpeed": null,
                "currentMTU": null,
                "maxMTU": null,
                "pciId": "0000:82:00.0",
                "pciFunction": null,
                "pciBusNumber": null
            }, {
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
                "macAddress": null,
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
            "name": "Solarflare SFC9140",
            "type": "ADAPTER",
            "id": "SFC9140",
            "versionController": "6.2.5.1000 rx1 tx1",
            "versionBootROM": "5.0.5.1002",
            "versionUEFIROM": "1.1.1.0",
            "versionFirmware": "1.1.1.0",
            "fileData": null,
            "status": null,
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
                "macAddress": null,
                "status": null,
                "interfaceName": null,
                "portSpeed": null,
                "currentMTU": null,
                "maxMTU": null,
                "pciId": "0000:04:00.0",
                "pciFunction": null,
                "pciBusNumber": null
            }, {
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
                "macAddress": null,
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



