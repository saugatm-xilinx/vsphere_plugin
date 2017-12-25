import {Component, ElementRef, ViewChild, OnInit, OnDestroy} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Http} from "@angular/http";
import {GlobalsService} from "../../../shared/globals.service";
import {Subscription} from 'rxjs/Subscription';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {isEmpty} from "rxjs/operator/isEmpty";

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
    public button = {
        latest: false,
        custom: false,
        latestErr: false,
        customErr: false
    };
    public updatable = {
        latest: true,
        custom: true
    };
    public selectedAdapters = [];
    public validateLatestUpdateModal = false;
    public customSelectUrl = false;
    customUploadFile: FormGroup;
    customUploadUrl: FormGroup;
    public customUpdate = {
        url: '',
        urlProtocol: ''
    };

    @ViewChild('fileInput') fileInput: ElementRef;

    constructor(private activatedRoute: ActivatedRoute,
                private http: Http,
                public gs: GlobalsService,
                private fb: FormBuilder) {
        this.activatedRoute.parent.params.subscribe((params: Params) => {
            this.params = params;
        });
        this.createFormFile();
        this.createFormUrl();
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
        if (!this.validateLatestUpdate)
            return false;
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
                    this.button.latest = false;
                    this.button.latestErr = false;
                },
                err => {
                    console.error(err);
                    this.button.latest = false;
                    this.button.latestErr = true;
                }
            );
    }


    createFormFile() {
        this.customUploadFile = this.fb.group({
            fwFile: ['', Validators.required],
        })
    }

    createFormUrl() {
        this.customUploadUrl = this.fb.group({
            url: ['', Validators.required],
            urlProtocol: ['', Validators.required],
        })
    }

    onFileChange(event) {
        let reader = new FileReader();
        if(event.target.files && event.target.files.length > 0) {
            let file = event.target.files[0];
            reader.readAsDataURL(file);
            reader.onload = () => {
                this.customUploadFile
                    .get('fwFile').setValue({
                    filename: file.name,
                    filetype: file.type,
                    value: reader.result.split(',')[1]
                })
            };
        }
    }

    onSubmitFile() {
        this.button.custom = true;
        const formModel = this.customUploadFile.value;
        let url = "";
        if (this.gs.isPluginMode()) {
            url = this.hostAdaptersListUrl + this.params['id'] + '/adapters/updateCustomWithBinary';
        } else {
            url = 'https://10.101.10.7/ui/solarflare/rest/services/hosts/' + this.params['id'] + '/adapters/updateCustomWithBinary';
        }
        let payload = {
            "url": null,
            "base64Data": formModel.fwFile.value,
            "adapters": this.selectedAdapters
        };
        this.http.post(url, payload)
            .subscribe(
                data => {
                    this.getAdapterList();
                    this.customUpdateModal = false;
                    this.button.custom = false;
                    this.button.customErr = false;
                    this.clearFile();
                },
                err => {
                    console.error(err);
                    this.clearFile();
                    setTimeout(() => {
                        this.button.custom = false;
                        this.button.customErr = true;
                    }, 1000);

                }
            );

    }


    onSubmitUrl() {
        this.button.custom = true;
        const formModel = this.customUploadUrl.value;
        let url = "";
        if (this.gs.isPluginMode()) {
            url = this.hostAdaptersListUrl + this.params['id'] + '/adapters/updateCustomWithUrl';
        } else {
            url = 'https://10.101.10.7/ui/solarflare/rest/services/hosts/' + this.params['id'] + '/adapters/updateCustomWithUrl';
        }
        let payload = {
            "url": formModel.urlProtocol + formModel.url,
            "base64Data": null,
            "adapters": this.selectedAdapters
        };
        this.http.post(url, payload)
            .subscribe(
                data => {
                    this.getAdapterList();
                    this.customUpdateModal = false;
                    this.button.custom = false;
                    this.button.customErr = false;
                },
                err => {
                    console.error(err);
                    setTimeout(() => {
                        this.button.custom = false;
                        this.button.customErr = true;
                        }, 1000);
                }
            );
    }

    clearFile() {
        this.customUploadFile.get('fwFile').setValue(null);
        this.fileInput.nativeElement.value = '';
        this.button.custom = false;
        this.button.customErr = false;
    }

    statusUpdate(statusList, column, retrn){
        let status = {};
        if (statusList.length === 0) return false;
        statusList.forEach((value) => {
            if (value.type === column ){
                status = value;
            }
        });
        if (status == {}) return false;
        if (Object.keys(status).length === 0)
            return false;
        let timestamp = new Date(status['timeStamp']);
        if (retrn === 'bool') {
            return status !== {};
        } else if (retrn === 'class') {
            return status['status'];
        } else if (retrn === 'txt') {
            return status['message'] + ' on ' + timestamp.toLocaleString();
        } else if(retrn === 'shape'){
            if (status['status'] === 'UPLOADING'){
                return "upload";
            }else if (status['status'] === 'UPLOADED'){
                return "check-circle";
            }else if (status['status'] === 'UPLOADING_FAIL'){
                return "exclamation-triangle";
            }else if (status['status'] === 'VALIDATING'){
                return "info-circle";
            }else if (status['status'] === 'VALIDATED'){
                return "check-circle";
            }else if (status['status'] === 'VALIDATION_FAIL'){
                return "exclamation-triangle";
            }else if(status['status'] == 'DONE'){
                return "success-standard";
            }
        }
        return false;
    }

    /*
    * 	UPLOADING,
    UPLOADED,
    UPLOADING_FAIL,
    VALIDATING,
    VALIDATED,
    VALIDATION_FAIL,
    DONE

    */
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
                "status": "UPLOADING",
                "message": "Uploading Controller firmware image",
                "timeStamp": 1513925798583,
                "type": "controller"
            },{
                "status": "UPLOADING_FAIL",
                "message": "Fail to update Controller firmware image",
                "timeStamp": 1513925798583,
                "type": "bootROM"
            },{
                "status": "UPLOADED",
                "message": "Uploaded Controller firmware image",
                "timeStamp": 1513925798583,
                "type": "UEFIROM"
            },],
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



