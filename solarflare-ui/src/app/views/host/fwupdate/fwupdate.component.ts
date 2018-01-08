import {Component, ElementRef, ViewChild, OnInit, OnDestroy, Injector, Input} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Http} from "@angular/http";
import {GlobalsService} from "../../../shared/globals.service";
import {Subscription} from 'rxjs/Subscription';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AppComponent} from "../../../app.component";
import {HostsService} from "../../../services/hosts.service";
import {Observable} from "rxjs/Observable";

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
    public validateCustomUpdateModal = false;
    public customSelectUrl = false;
    customUploadFile: FormGroup;
    customUploadUrl: FormGroup;
    public hosts = [];
    public gettingAdapterList = false;
    public close = false;
    public status = {
        latest: {
            modal: false,
            output: [],
            obs: null
        },
        custom: {
            modal: false,
            output: [],
            obs: null
        },
        selectedAdapters: [],
        status: null
    };
    public getAdapterListErr = false;

    @ViewChild('fileInput') fileInput: ElementRef;

    constructor(private activatedRoute: ActivatedRoute,
                private http: Http,
                private gs: GlobalsService,
                private fb: FormBuilder,
                private inj: Injector,
                private hs: HostsService) {
        this.activatedRoute.parent.params.subscribe((params: Params) => {
            this.params = params;
        });
        this.createFormFile();
        this.createFormUrl();
        this.hosts = this.inj.get(AppComponent).hosts;
    }

    static returnStatusOutput(br){
        if (br.state === 'Error'){
            return {
                state: br.state,
                message: br.errorMessage
            }
        }else if (br.state === 'Success'){
            return {
                state: br.state,
                message: null
            }
        }else if(br.state === 'Running'){
            return {
                state: br.state,
                message: null
            }
        }else if(br.state === 'Queued'){
            return {
                state: br.state,
                message: null
            }
        }
    }

    reInitStatus(){
        this.status = {
            latest: {
                modal: false,
                output: [],
                obs: null
            },
            custom: {
                modal: false,
                output: [],
                obs: null
            },
            selectedAdapters: [],
            status: null
        };
    }

    getAdapterList() {
        this.adapterList = [];
        this.status.status = false;
        this.gettingAdapterList = true;
        this.getAdapterListErr = false;
        this.hs.getAdapters(this.params['id'])
            .subscribe(
                data => {
                    this.adapterList = data;
                    this.gettingAdapterList = false;
                    this.updateStatus(data);
                },
                err => {
                    console.error(err);
                    this.gettingAdapterList = false;
                    this.getAdapterListErr = true;
                    //setTimeout(this.devMode(),400);
                }
            );
    }

    updateStatus(data){
        if (this.status.custom.output && this.status.custom.output.length > 0){
            this.status.custom.output.forEach((op, i ) => {
                data.forEach((adapter, j) => {
                    if (adapter.id === op.id){

                        if (op.bootRom !== null) {
                            this.status.custom.output[i].bootRom.to = adapter.versionBootROM;
                        }
                        if (op.controller !== null) {
                            this.status.custom.output[i].controller.to = adapter.versionController;
                        }
                        if (op.uefiRom !== null) {
                            this.status.custom.output[i].uefiRom.to = adapter.versionUEFIROM;
                        }

                    }
                })
            })
        }
    }

    ngOnInit() {
        this.getAdapterList();
        //this.devMode();
    }

    validateLatestUpdate(remove: string) {
        let updatable = 0, invalid = 0, filterdAdapters = [];
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

    validateCustomUpdate() {
        let sa = this.selectedAdapters;
        let updatable = 0;
        if (sa.length === 0) {
            return false
        }
        let model = sa[0];
        sa.forEach(item => {
            if (model['deviceId'] !== item['deviceId']) {
                updatable--;
            }
        });

        if (updatable === 0) {
            this.updatable.custom = true;
            this.customUpdateModal = true;
            this.button.custom = false;
            this.button.customErr = false;
            this.validateCustomUpdateModal = false;
            return true;
        } else {
            this.updatable.custom = false;
            this.customUpdateModal = false;
            this.validateCustomUpdateModal = true;
            setTimeout(() => {
                this.close = true;
            }, 3000);
            return false;
        }
    }

    latestUpdate() {
        /*        if (!this.validateLatestUpdate)
                    return false;*/

        this.hs.latestUpdate(this.params['id'], this.selectedAdapters)
            .subscribe(
                data => {
                    console.log(data);
                    this.reInitStatus();

                    this.latestUpdateModal = false;
                    this.status.latest.modal = true;
                    this.status.selectedAdapters = this.selectedAdapters;
                    this.getLatestUpdateStatus(this.selectedAdapters, data.taskId);

                    this.getAdapterList();
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
        if (event.target.files && event.target.files.length > 0) {
            let file = event.target.files[0];
            reader.readAsDataURL(file);
            reader.onload = () => {
                this.customUploadFile
                    .get('fwFile').setValue({
                    filename: file.name,
                    filetype: file.type,
                    value: reader.result.split(',')[1]
                });
            };
        }
    }

    onSubmitFile() {
        if (!this.validateCustomUpdate())
            return false;

        this.button.custom = true;
        const formModel = this.customUploadFile.value;

        let payload = {
            "url": null,
            "base64Data": formModel.fwFile.value,
            "adapters": this.selectedAdapters
        };

        this.hs.onSubmitFile(this.params['id'], payload)
            .subscribe(
                data => {
                    this.reInitStatus();
                    this.customUpdateModal = false;
                    this.status.custom.modal = true;
                    this.status.selectedAdapters = this.selectedAdapters;
                    this.getCustomUpdateStatus(this.selectedAdapters, data.taskId);

                    //this.getAdapterList();
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

        if (!this.validateCustomUpdate())
            return false;

        this.button.custom = true;
        const formModel = this.customUploadUrl.value;

        let payload = {
            "url": formModel.urlProtocol + formModel.url,
            "base64Data": null,
            "adapters": this.selectedAdapters
        };

        this.hs.onSubmitUrl(this.params['id'], payload)
            .subscribe(
                data => {
                    this.reInitStatus();
                    this.customUpdateModal = false;
                    this.status.custom.modal = true;
                    this.status.selectedAdapters = this.selectedAdapters;
                    this.getCustomUpdateStatus(this.selectedAdapters, data.taskId);

                    //this.getAdapterList();
                    this.button.custom = false;
                    this.button.customErr = false;
                    this.customUploadUrl.get('url').setValue('');
                    this.customUploadUrl.get('urlProtocol').setValue('');
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

    statusUpdate(statusList, column, retrn) {
        let status = {};
        if (statusList.length === 0) return false;
        statusList.forEach((value) => {
            if (value.type === column) {
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
        } else if (retrn === 'shape') {
            if (status['status'] === 'UPLOADING') {
                return "upload";
            } else if (status['status'] === 'UPLOADED') {
                return "check-circle";
            } else if (status['status'] === 'UPLOADING_FAIL') {
                return "exclamation-triangle";
            } else if (status['status'] === 'VALIDATING') {
                return "info-circle";
            } else if (status['status'] === 'VALIDATED') {
                return "check-circle";
            } else if (status['status'] === 'VALIDATION_FAIL') {
                return "exclamation-triangle";
            } else if (status['status'] == 'DONE') {
                return "success-standard";
            }
        }
        return false;
    }

    urlVerifier() {
        let url = this.customUploadUrl.get('url');
        let re = /^(https?:\/\/)/;
        let rehttp = /^(http:\/\/)/;
        let rehttps = /^(https:\/\/)/;
        let retftp = /^(tftp:\/\/)/;
        let reftp = /^(ftp:\/\/)/;
        let resftp = /^(sftp:\/\/)/;
        if (url.status !== 'INVALID') {
            if (rehttp.test(url.value)) {
                this.customUploadUrl.get('url').setValue(url.value.replace(rehttp, ""));
                this.customUploadUrl.get('urlProtocol').setValue('http://');
            } else if (rehttps.test(url.value)) {
                this.customUploadUrl.get('url').setValue(url.value.replace(rehttps, ""));
                this.customUploadUrl.get('urlProtocol').setValue('https://');
            } else if (retftp.test(url.value)) {
                this.customUploadUrl.get('url').setValue(url.value.replace(retftp, ""));
                this.customUploadUrl.get('urlProtocol').setValue('tftp://');
            } else if (resftp.test(url.value)) {
                this.customUploadUrl.get('url').setValue(url.value.replace(resftp, ""));
                this.customUploadUrl.get('urlProtocol').setValue('sftp://');
            } else if (reftp.test(url.value)) {
                this.customUploadUrl.get('url').setValue(url.value.replace(reftp, ""));
                this.customUploadUrl.get('urlProtocol').setValue('ftp://');
            }
        } else {
            return true;
        }
    }

    getLatestUpdateStatus(adapters: object, taskId: string) {

        let obs = Observable.interval(1000)
            .switchMap(() => this.hs.getStatus(taskId).map((data) => data))
            .subscribe((data) => {
                    if (this.status.status === true) {
                        this.status.status = false;
                        obs.unsubscribe();
                    }else{
                        this.processStatusLatest(data, adapters);
                    }
                },
                err => {
                    console.log(err);
                });

    }

    updateStatusOutput(a){
        let total = 0, current = 0;
        this.status[a].output.forEach((i,j) => {
            if (i['controller'] !== null ){
                total++;
                if(i['controller']['state'] === 'Success' || i['controller']['state'] === 'Error')
                    current++;
            }
            if (i['bootRom'] !== null ){
                total++;
                if(i['bootRom']['state'] === 'Success' || i['controller']['state'] === 'Error')
                    current++;
            }
            if (i['uefiRom'] !== null ){
                total++;
                if(i['uefiRom']['state'] === 'Success' || i['controller']['state'] === 'Error')
                    current++;
            }

            if (total !== 0 && total === current)
                this.status.status = true;
        });
    }

    processStatusLatest(status, adapters){
        //this.status.latest.output = [];
        this.updateStatusOutput('latest');
        //this.status.latest.data = status.adapterTasks;

        adapters.forEach((adapter, index) => {
            //console.log(adapter);

            this.status.latest.output[index] = {
                id: adapter.id,
                name: adapter.name,
                bootRom: null,
                controller: null,
                uefiRom: null
            };
            if (status && status.adapterTasks && status.adapterTasks.length > 0 ) {
                status.adapterTasks.forEach((task, i) => {
                    if (task['adapterId'] === adapter.id) {
                        if (task['bootROM'] !== null) {
                            let br = task['bootROM'];
                            this.status.latest.output[index].bootRom = FwupdateComponent.returnStatusOutput(br);
                            this.status.latest.output[index].bootRom.from = adapter.versionBootROM;
                            this.status.latest.output[index].bootRom.to = adapter.latestVersion.bootROM;
                        }
                        if (task['controller'] !== null) {
                            let br = task['controller'];
                            this.status.latest.output[index].controller = FwupdateComponent.returnStatusOutput(br);
                            this.status.latest.output[index].controller.from = adapter.versionController;
                            this.status.latest.output[index].controller.to = adapter.latestVersion.controller;
                        }
                        if (task['uefiROM'] !== null) {
                            let br = task['uefiROM'];
                            this.status.latest.output[index].uefiRom = FwupdateComponent.returnStatusOutput(br);
                            this.status.latest.output[index].uefiRom.from = adapter.versionUEFIROM;
                            this.status.latest.output[index].uefiRom.to = adapter.latestVersion.uefi;
                        }
                    }
                });
            }
        });
    }

    getCustomUpdateStatus(adapters: object, taskId: string) {
        this.getAdapterList();
        let obs = Observable.interval(1000)
            .switchMap(() => this.hs.getStatus(taskId).map((data) => data))
            .subscribe((data) => {
                    if (this.status.status === true) {
                        this.status.status = false;
                        obs.unsubscribe();
                    }else{
                        this.processStatusCustom(data, adapters);
                    }
                },
                err => {
                    console.log(err);
                });


    }

    processStatusCustom(status, adapters){
        //this.status.custom.output = [];
        this.updateStatusOutput('custom');
        //this.status.custom.data = status.adapterTasks;

        adapters.forEach((adapter, index) => {
            //console.log(adapter);

            this.status.custom.output[index] = {
                id: adapter.id,
                name: adapter.name,
                bootRom: null,
                controller: null,
                uefiRom: null
            };
            if (status && status.adapterTasks && status.adapterTasks.length > 0 ) {
                status.adapterTasks.forEach((task, i) => {
                    if (task['adapterId'] === adapter.id) {
                        if (task['bootROM'] !== null) {
                            let br = task['bootROM'];
                            this.status.custom.output[index].bootRom = FwupdateComponent.returnStatusOutput(br);
                            this.status.custom.output[index].bootRom.from = adapter.versionBootROM;
                        }
                        if (task['controller'] !== null) {
                            let br = task['controller'];
                            this.status.custom.output[index].controller = FwupdateComponent.returnStatusOutput(br);
                            this.status.custom.output[index].controller.from = adapter.versionController;
                        }
                        if (task['uefiROM'] !== null) {
                            let br = task['uefiROM'];
                            this.status.custom.output[index].uefiRom = FwupdateComponent.returnStatusOutput(br);
                            this.status.custom.output[index].uefiRom.from = adapter.versionUEFIROM;
                        }
                    }
                });
            }
        });
    }

    devMode() {
        this.adapterList = [{
            "name": "SFC9220-000f534b6650",
            "type": "ADAPTER",
            "id": "SFC9220",
            "versionController": "6.2.0.1016 rx1 tx1",
            "versionBootROM": "0.0.0.0",
            "versionUEFIROM": "1.1.1.0",
            "versionFirmware": "1.1.1.0",
            "latestVersion": {
                "controlerVersion": "0.0.0.0",
                "bootROMVersion": "0.0.0.0",
                "uefiVersion": "1.1.1.0",
                "firmewareFamilyVersion": "1.1.1.0"
            },
            "status": [{
                "status": "VALIDATING",
                "message": "Controller firmware image validation in-progress",
                "timeStamp": 1514444024640,
                "type": "controller"
            }, {
                "status": "VALIDATING",
                "message": "BootROM firmware image validation in-progress",
                "timeStamp": 1514395891360,
                "type": "BootROM"
            }],
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
            "name": "SFC9140-000f532fbf20",
            "type": "ADAPTER",
            "id": "SFC9140",
            "versionController": "6.2.7.1000 rx1 tx1",
            "versionBootROM": "5.0.5.1002",
            "versionUEFIROM": "1.1.1.0",
            "versionFirmware": "1.1.1.0",
            "latestVersion": {
                "controlerVersion": "6.2.7.1000",
                "bootROMVersion": "5.0.5.1002",
                "uefiVersion": "1.1.1.0",
                "firmewareFamilyVersion": "1.1.1.0"
            },
            "status": [{
                "status": "DONE",
                "message": "Controller firmware image update is done",
                "timeStamp": 1514449874980,
                "type": "controller"
            }, {
                "status": "DONE",
                "message": "BootROM firmware image update is done",
                "timeStamp": 1514449884356,
                "type": "BootROM"
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
            "laterVersionAvailable": false
        }];
    }

}



