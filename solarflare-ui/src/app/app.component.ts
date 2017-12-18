import {Component, Injector, ChangeDetectorRef} from "@angular/core";

import {GlobalsService, RefreshService, I18nService} from "./shared/index";
import {ActionDevService} from "./services/testing/action-dev.service";
import {AppMainService} from "./services/app-main.service";
import {Http} from "@angular/http";

@Component({
    selector: "my-app",
    styleUrls: ["./app.component.scss"],
    templateUrl: "./app.component.html",
    providers: []
})

export class AppComponent {
    public hosts: any;
    public allHostUrl = this.gs.getWebContextPath() + '/rest/services/hosts/';

    constructor(public  gs: GlobalsService,
                private injector: Injector,
                private refreshService: RefreshService,
                private i18nService: I18nService,
                private changeDetector: ChangeDetectorRef,
                private http: Http) {

        // Refresh handler to be used in plugin mode
        this.gs.getWebPlatform().setGlobalRefreshHandler(this.refresh.bind(this), document);

        // Manual injection of ActionDevService, used in webPlatformStub
        if (!this.gs.isPluginMode()) {
            this.injector.get(ActionDevService);
        }

        // Start the app in english by default (dev mode)
        // In plugin mode the current locale is passed as parameter
        this.i18nService.initLocale("en");
    }

    ngOnInit(): void {
        let url = "";
        if (this.gs.isPluginMode()) {
            url = this.allHostUrl;
        } else {
            url = 'https://10.101.10.7/ui/solarflare/rest/services/hosts/';
        }
        this.http.get(url)
            .subscribe(
                data => {
                    this.hosts = data.json()
                },
                err => {
                    console.error(err);
                    this.devMode();
                }
            );
    }

    devMode(){
        this.hosts = [{
            "type": "HOST",
            "id": "host-9",
            "name": "10.101.10.3",
            "children": [{
                "name": "Solarflare SFC9220",
                "type": "ADAPTER",
                "id": "Solarflare SFC9220",
                "versionController": null,
                "versionBootROM": null,
                "versionUEFIROM": null,
                "versionFirmware": null,
                "fileData": null,
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
                "id": "Solarflare SFC9140",
                "versionController": null,
                "versionBootROM": null,
                "versionUEFIROM": null,
                "versionFirmware": null,
                "fileData": null,
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
                "laterVersionAvailable": false
            }],
            "adapterCount": "2",
            "portCount": "1",
            "driverVersion": "444",
            "cimProviderVersion": "2222"
        }];
    }



    refresh(): void {
        // This propagates the refresh event to views that have subscribed to the RefreshService
        this.refreshService.refreshView();

        if (this.gs.isPluginMode()) {
            // This helps refresh the app's children components in Plugin mode after refreshView
            this.changeDetector.detectChanges();
        }
    }
}





