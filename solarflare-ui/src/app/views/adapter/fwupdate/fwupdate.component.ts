import { Component, ElementRef, ViewChild, OnInit, OnDestroy, Injector, Input } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { AdapterService } from "../../../services/adapter.service";
import { GlobalsService } from "../../../shared/globals.service";
import { HostsService } from "../../../services/hosts.service";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { Observable } from "rxjs/Observable";
import { environment } from "environments/environment"

@Component({
    selector: 'app-fwupdate',
    templateUrl: './fwupdate.component.html',
    styleUrls: ['./fwupdate.component.scss']
})
export class FwupdateComponent implements OnInit {
    public isProd = environment.production
    public params = {};
    public adapter = {};
    public adapters = [];
    public gettingAdapterList = false;
    public getAdapterListErr = false;
    public latestUpdateModal = false;
    public customUpdateModal = false;
    public errText = ""
    public button = {
        latest: false,
        custom: false,
        latestErr: false,
        customErr: false
    };
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
    public customSelectUrl = false;
    customUploadFile: FormGroup;
    customUploadUrl: FormGroup;
    public statusUpdate = false;
    public dots = '.';
    public refreshButtonDisabled;

    @ViewChild('fileInput') fileInput: ElementRef;

    constructor(private activatedRoute: ActivatedRoute,
        public gs: GlobalsService,
        private fb: FormBuilder,
        private as: AdapterService,
        private hs: HostsService) {
        this.activatedRoute.parent.params.subscribe((params: Params) => {
            this.params = params;
        });
        this.createFormFile();
        this.createFormUrl();
    }

    getAdapterList() {
        this.getAdapterListErr = false;
        this.refreshButtonDisabled = true
        this.hs.getAdapters(this.params['hostid'])
            .subscribe(
                data => {
                    this.refreshButtonDisabled = false
                    this.findAdapter(data);
                    this.gettingAdapterList = false;
                    this.updateStatus(data);
                },
                err => {
                    console.error(err);
                    this.refreshButtonDisabled = false
                    this.gettingAdapterList = false;
                    this.getAdapterListErr = true;
                }
            );
    }

    findAdapter(data) {
        data.forEach((adapter, index) => {
            adapter.children.forEach(nic => {
                if (nic.id === this.params['nicId']) {
                    this.adapter = adapter;
                    this.adapters = [adapter];
                }
            })
        })
    }

    ngOnInit() {
        this.getAdapterList();
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
    
    latestUpdate() {

        this.hs.latestUpdate(this.params['hostid'], [this.adapter])
            .subscribe(
                data => {
                    this.reInitStatus();

                    this.latestUpdateModal = false;
                    this.status.latest.modal = true;
                    this.status.selectedAdapters.push(this.adapter);
                    this.getLatestUpdateStatus([this.adapter], data.taskId);

                    this.getAdapterList();
                    this.button.latest = false;
                    this.button.latestErr = false;
                },
                err => {
                    console.error(err);
                    const error = err.json();
                    this.errText = error ? error.message : null;
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
        this.button.custom = true;
        const formModel = this.customUploadFile.value;

        const payload = {
            "url": null,
            "base64Data": formModel.fwFile.value,
            "adapters": [this.adapter]
        };

        this.hs.onSubmitFile(this.params['hostid'], payload)
            .subscribe(
                data => {
                    this.reInitStatus();
                    this.customUpdateModal = false;
                    this.status.custom.modal = true;
                    this.status.selectedAdapters.push(this.adapter);
                    this.getCustomUpdateStatus([this.adapter], data.taskId);
                    this.button.custom = false;
                    this.button.customErr = false;
                    this.clearFile();
                },
                err => {
                    console.error(err);
                    const error = err.json();
                    this.errText = error ? error.message : null;
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

        const payload = {
            "url": formModel.urlProtocol + formModel.url,
            "base64Data": null,
            "adapters": [this.adapter]
        };

        this.hs.onSubmitUrl(this.params['hostid'], payload)
            .subscribe(
                data => {
                    this.reInitStatus();
                    this.customUpdateModal = false;
                    this.status.custom.modal = true;
                    this.status.selectedAdapters.push(this.adapter);
                    this.getCustomUpdateStatus([this.adapter], data.taskId);

                    this.button.custom = false;
                    this.button.customErr = false;
                    this.customUploadUrl.get('url').setValue('');
                    this.customUploadUrl.get('urlProtocol').setValue('');
                },
                err => {
                    console.error(err);
                    const error = err.json();
                    this.errText = error ? error.message : null;
                    setTimeout(() => {
                        this.button.custom = false;
                        this.button.customErr = true;
                    }, 1000);
                }
            );
    }

    clearFile() {
        this.customUploadFile.get('fwFile').setValue(null);
        if(this.fileInput && this.fileInput.nativeElement){
            this.fileInput.nativeElement.value = '';
        }
        this.button.custom = false;
        this.button.customErr = false;
        this.errText = ""
    }

    cancelUpdateModal() {
        this.customUpdateModal = false;
        this.clearFile();
        this.customUploadUrl.get('url').setValue('');
        this.customUploadUrl.get('urlProtocol').setValue('');
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
                    const error = err.json();
                    this.errText = error ? error.message : null;
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

        this.updateStatusOutput('latest');

        adapters.forEach((adapter, index) => {

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
                    const error = err.json();
                    this.errText = error ? error.message : null;
                    console.log(err);
                });


    }

    processStatusCustom(status, adapters) {

        this.updateStatusOutput('custom');

        adapters.forEach((adapter, index) => {

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
}
