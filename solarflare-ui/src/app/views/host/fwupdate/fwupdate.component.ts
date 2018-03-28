import { Component, ElementRef, ViewChild, OnInit, OnDestroy, Injector, Input } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Http } from "@angular/http";
import { GlobalsService } from "../../../shared/globals.service";
import { Subscription } from 'rxjs/Subscription';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { AppComponent } from "../../../app.component";
import { HostsService } from "../../../services/hosts.service";
import { Observable } from "rxjs/Observable";
import { environment } from "environments/environment"

//  TODO: review comments - components should not have this much line code in a single file.
//  Break code in smaller chunck and may create a helper/service to serve functionality.
//  Ideal code line should be 200-300 max in a single file
//  F/w Update component functionality requires this amount of code and helper methods
//  has been implemented wherever possible.

@Component({
    selector: 'app-fwupdate',
    templateUrl: './fwupdate.component.html',
    styleUrls: ['./fwupdate.component.scss']
})
export class FwupdateComponent implements OnInit {
    public isProd = environment.production
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
        status: false
    };
    public getAdapterListErr = false;
    public latestUpdateAdapterFilter = false;
    public statusUpdate = false;
    public dots = '.';

    @ViewChild('fileInput') fileInput: ElementRef;

    customUpdateErrorMessage = '';
    fetchDataErrorMessage = '';

    constructor(private activatedRoute: ActivatedRoute,
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

    ngOnInit() {
        const adapterDetail = this.hs.getAdapter(this.params['id']);
        if (adapterDetail && adapterDetail.isLatest) {
            this.adapterList = adapterDetail.adapters;
            this.updateStatus(this.adapterList);
        } else {
            this.getAdapterList();
        }
        // this.devMode();
    }

    static returnStatusOutput(br) {
        if (br.state === 'Error') {
            return {
                state: br.state,
                message: br.errorMessage
            }
        } else if (br.state === 'Success') {
            return {
                state: br.state,
                message: null
            }
        } else if (br.state === 'Running') {
            return {
                state: br.state,
                message: null
            }
        } else if (br.state === 'Queued') {
            return {
                state: br.state,
                message: null
            }
        }
    }

    reInitStatus() {
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
            status: false
        };
    }

    refreshAdapterList() {
        this.adapterList = [];
        this.getAdapterList();
    }

    getAdapterList() {
        this.status.status = false;
        this.gettingAdapterList = true;
        this.getAdapterListErr = false;
        this.fetchDataErrorMessage = '';
        this.hs.getAdapters(this.params['id'])
            .subscribe(
                data => {
                    this.adapterList = data;
                    this.hs.setAdapter({ hostId: this.params['id'], adapters: data, isLatest: true });
                    this.gettingAdapterList = false;
                    this.updateStatus(data);
                },
                err => {
                    const error = err.json();
                    this.fetchDataErrorMessage = error ? error.message : null;
                    console.error(err);
                    this.gettingAdapterList = false;
                    this.getAdapterListErr = true;
                    // setTimeout(this.devMode(),400);
                }
            );
    }

    updateStatus(data) {
        if (this.status.custom.output && this.status.custom.output.length > 0) {
            this.status.custom.output.forEach((op, i) => {
                data.forEach((adapter, j) => {
                    if (adapter.id === op.id) {
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
            });
            this.statusUpdate = false;
        }
    }

    validateLatestUpdate(remove?: string) {
        let updatable = 0, invalid = 0;
        const filterdAdapters = [];
        this.selectedAdapters.forEach((value, index) => {
            if (value.latestVersion.controller !== value.versionController.split(' ')[0]) {
                updatable++;
            } else if (value.latestVersion.bootROM !== value.versionBootROM) {
                updatable++;
            } else if (value.latestVersion.uefi !== value.versionUEFIROM) {
                updatable++;
            } else {
                invalid++;
            }
        });

        if (remove === 'remove') {
            this.selectedAdapters.forEach((value, index) => {
                if (value.latestVersion.controller !== value.versionController.split(' ')[0]) {
                    filterdAdapters.push(value);
                } else if (value.latestVersion.bootROM !== value.versionBootROM) {
                    filterdAdapters.push(value);
                } else if (value.latestVersion.uefi !== value.versionUEFIROM) {
                    filterdAdapters.push(value);
                }
            });
            this.selectedAdapters = filterdAdapters;
            if (this.selectedAdapters && this.selectedAdapters.length !== 0) {
                return true;
            }
        }

        updatable !== 0 ? this.updatable.latest = true : this.updatable.latest = false;

        return updatable !== 0;
    }

    latestUpdateButton() {
        if (this.validateLatestUpdate()) {
            return true;
        } else {
            return this.latestUpdateAdapterFilter === true;
        }
    }

    validateCustomUpdate() {
        const sa = this.selectedAdapters;
        let updatable = 0;
        if (sa.length === 0) {
            return false
        }
        const model = sa[0];
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

    isLatestAvailable(ad) {
        if (!ad.latestVersion.controller.includes(ad.versionController.split(' ')[0])) {
            return "Yes";
        } else if (!ad.latestVersion.bootROM.includes(ad.versionBootROM)) {
            return "Yes";
        } else if (!ad.latestVersion.uefi.includes(ad.versionUEFIROM)) {
            return "Yes";
        } else {
            return "No";
        }
    }

    disableLatestUpdateAdapterFilter() {
        const result = this.selectedAdapters.find(ele => {
            return this.isLatestAvailable(ele) === 'No';
        });
        return result ? false : true;
    }

    disableUpdateButton() {
        const updatable = this.selectedAdapters.filter(ele => {
            return this.isLatestAvailable(ele) === 'No';
        });
        return updatable.length === this.selectedAdapters.length ? true : false;
    }

    closeAndResetModal() {
        this.customUpdateModal = false;
        this.customUploadFile.reset();
        this.customUploadUrl.reset();
    }

    latestUpdate() {
        this.customUpdateErrorMessage = '';
        if (!this.latestUpdateAdapterFilter) {
            this.validateLatestUpdate('remove');
        }
        this.hs.latestUpdate(this.params['id'], this.selectedAdapters)
            .subscribe(
                data => {
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
                    const error = err.json();
                    this.customUpdateErrorMessage = error ? error.message : null;
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
        const reader = new FileReader();
        if (event.target.files && event.target.files.length > 0) {
            const file = event.target.files[0];
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
        if (!this.validateCustomUpdate()) {
            return false;
        }

        this.button.custom = true;
        const formModel = this.customUploadFile.value;

        const payload = {
            "url": null,
            "base64Data": formModel.fwFile.value,
            "adapters": this.selectedAdapters
        };
        this.customUpdateErrorMessage = '';
        this.hs.onSubmitFile(this.params['id'], payload)
            .subscribe(
                data => {
                    this.reInitStatus();
                    this.customUpdateModal = false;
                    this.status.custom.modal = true;
                    this.status.selectedAdapters = this.selectedAdapters;
                    this.getCustomUpdateStatus(this.selectedAdapters, data.taskId);

                    // this.getAdapterList();
                    this.button.custom = false;
                    this.button.customErr = false;
                    this.clearFile();
                },
                err => {
                    const error = err.json();
                    this.customUpdateErrorMessage = error ? error.message : null;
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

        if (!this.validateCustomUpdate()) {
            return false;
        }

        this.button.custom = true;
        const formModel = this.customUploadUrl.value;

        const payload = {
            "url": formModel.urlProtocol + formModel.url,
            "base64Data": null,
            "adapters": this.selectedAdapters
        };
        this.customUpdateErrorMessage = '';
        this.hs.onSubmitUrl(this.params['id'], payload)
            .subscribe(
                data => {
                    this.reInitStatus();
                    this.customUpdateModal = false;
                    this.status.custom.modal = true;
                    this.status.selectedAdapters = this.selectedAdapters;
                    this.getCustomUpdateStatus(this.selectedAdapters, data.taskId);

                    // this.getAdapterList();
                    this.button.custom = false;
                    this.button.customErr = false;
                    this.customUploadUrl.get('url').setValue('');
                    this.customUploadUrl.get('urlProtocol').setValue('');
                },
                err => {
                    const error = err.json();
                    this.customUpdateErrorMessage = error ? error.message : null;
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

    urlVerifier() {
        const url = this.customUploadUrl.get('url');
        const re = /^(https?:\/\/)/;
        const rehttp = /^(http:\/\/)/;
        const rehttps = /^(https:\/\/)/;
        const retftp = /^(tftp:\/\/)/;
        const reftp = /^(ftp:\/\/)/;
        const resftp = /^(sftp:\/\/)/;
        if (url.status !== 'INVALID') {
            if (rehttp.test(url.value)) {
                this.customUploadUrl.get('url').setValue(url.value.replace(rehttp, ""));
                this.customUploadUrl.get('urlProtocol').setValue('http://');
            } else if (rehttps.test(url.value)) {
                this.customUploadUrl.get('url').setValue(url.value.replace(rehttps, ""));
                this.customUploadUrl.get('urlProtocol').setValue('https://');
            } else if (reftp.test(url.value)) {
                this.customUploadUrl.get('url').setValue(url.value.replace(reftp, ""));
                this.customUploadUrl.get('urlProtocol').setValue('ftp://');
            }
        } else {
            return true;
        }
    }

    getLatestUpdateStatus(adapters: object, taskId: string) {
        this.statusUpdate = true;
        this.dots = '.';
        const obs = Observable.interval(3000)
            .switchMap(() => this.hs.getStatus(taskId).map((data) => data))
            .subscribe((data) => {
                this.dots = this.dots + '.';
                if (this.status.status === true) {
                    this.status.status = false;
                    this.processStatusLatest(data, adapters);
                    this.statusUpdate = false;
                    obs.unsubscribe();
                } else {
                    this.processStatusLatest(data, adapters);
                }
            },
                err => {
                    console.log(err);
                });
    }

    updateStatusOutput(a) {
        let total = 0, current = 0;
        this.status[a].output.forEach((i, j) => {
            if (i['controller'] !== null) {
                total++;
                if (i['controller']['state'] === 'Success' || i['controller']['state'] === 'Error') {
                    current++;
                }
            }
            if (i['bootRom'] !== null) {
                total++;
                if (i['bootRom']['state'] === 'Success' || i['bootRom']['state'] === 'Error') {
                    current++;
                }
            }
            if (i['uefiRom'] !== null) {
                total++;
                if (i['uefiRom']['state'] === 'Success' || i['uefiRom']['state'] === 'Error') {
                    current++;
                }
            }

            if (total !== 0 && (this.status[a].output.length === (j + 1)) && total === current) {
                this.status.status = true;
            }
        });
    }

    processStatusLatest(status, adapters) {
        // this.status.latest.output = [];
        this.updateStatusOutput('latest');
        // this.status.latest.data = status.adapterTasks;

        adapters.forEach((adapter, index) => {
            // console.log(adapter);

            this.status.latest.output[index] = {
                id: adapter.id,
                name: adapter.name,
                bootRom: null,
                controller: null,
                uefiRom: null
            };
            if (status && status.adapterTasks && status.adapterTasks.length > 0) {
                status.adapterTasks.forEach((task, i) => {
                    if (task['adapterId'] === adapter.id) {
                        if (task['bootROM'] !== null) {
                            const br = task['bootROM'];
                            this.status.latest.output[index].bootRom = FwupdateComponent.returnStatusOutput(br);
                            this.status.latest.output[index].bootRom.from = adapter.versionBootROM;
                            this.status.latest.output[index].bootRom.to = adapter.latestVersion.bootROM;
                        }
                        if (task['controller'] !== null) {
                            const br = task['controller'];
                            this.status.latest.output[index].controller = FwupdateComponent.returnStatusOutput(br);
                            this.status.latest.output[index].controller.from = adapter.versionController;
                            this.status.latest.output[index].controller.to = adapter.latestVersion.controller;
                        }
                        if (task['uefiROM'] !== null) {
                            const br = task['uefiROM'];
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
        this.statusUpdate = true;
        this.dots = '.';
        const obs = Observable.interval(3000)
            .switchMap(() => this.hs.getStatus(taskId).map((data) => data))
            .subscribe((data) => {
                this.dots = this.dots + '.';
                if (this.status.status === true) {
                    this.status.status = false;
                    this.processStatusCustom(data, adapters);
                    this.getAdapterList();
                    obs.unsubscribe();
                } else {
                    this.processStatusCustom(data, adapters);
                }
            },
                err => {
                    console.log(err);
                });


    }

    processStatusCustom(status, adapters) {
        // this.status.custom.output = [];
        this.updateStatusOutput('custom');
        // this.status.custom.data = status.adapterTasks;

        adapters.forEach((adapter, index) => {
            // console.log(adapter);

            this.status.custom.output[index] = {
                id: adapter.id,
                name: adapter.name,
                bootRom: null,
                controller: null,
                uefiRom: null
            };
            if (status && status.adapterTasks && status.adapterTasks.length > 0) {
                status.adapterTasks.forEach((task, i) => {
                    if (task['adapterId'] === adapter.id) {
                        if (task['bootROM'] !== null) {
                            const br = task['bootROM'];
                            this.status.custom.output[index].bootRom = FwupdateComponent.returnStatusOutput(br);
                            this.status.custom.output[index].bootRom.from = adapter.versionBootROM;
                        }
                        if (task['controller'] !== null) {
                            const br = task['controller'];
                            this.status.custom.output[index].controller = FwupdateComponent.returnStatusOutput(br);
                            this.status.custom.output[index].controller.from = adapter.versionController;
                        }
                        if (task['uefiROM'] !== null) {
                            const br = task['uefiROM'];
                            this.status.custom.output[index].uefiRom = FwupdateComponent.returnStatusOutput(br);
                            this.status.custom.output[index].uefiRom.from = adapter.versionUEFIROM;
                        }
                    }
                });
            }
        });
    }

    resetView() {
        this.customUpdateErrorMessage = '';
        this.button.customErr = false;
    }
}



