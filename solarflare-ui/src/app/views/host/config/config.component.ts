import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { HostsService } from "../../../services/hosts.service";
import { GlobalsService } from "../../../shared/globals.service";
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";
import { environment } from "environments/environment"
// TODO :- reveiw comments - It would be best if import is done via a absolute path.
// Define all modules paths in tsconfig.json file. This will help in managing file import.
// Import definitions are auto generated as plugin paths are relatively packaged with vmware web client.

@Component({
    selector: 'app-config',
    templateUrl: './config.component.html',
    styleUrls: ['./config.component.scss']
})
export class ConfigComponent implements OnInit {
    public isProd = environment.production
    public params = {};
    public refreshButtonDisabled;
    hostConfig: FormGroup;
    public configDefault = {
        "netQueue": { "netQueueCount": 8, "rss": 4, "maxNumpCPU": false },
        "debuggingMask": {
            "utils": true,
            "mgmt": false,
            "uplink": false,
            "transmit": false,
            "receive": true,
            "hardware": false,
            "eventQueue": false,
            "rss": false,
            "port": false,
            "interrupt": false,
            "commonCode": false,
            "driver": false,
            "filter": false,
            "mcdi": false
        },
        "overlay": { "vxlanOffloadEnable": true, "geneveOffloadEnable": true },
        "restart": false
    };
    public config: any = {};
    public devModeConfig = {};
    public err = {
        getConfiguration: false,
    };
    public btn = {
        netQueue: {
            count: false,
            rss: false,
        },
        restore: false,
        configDefault: false
    };
    public submitted = false;

    constructor(private activatedRoute: ActivatedRoute,
        public gs: GlobalsService,
        private hs: HostsService) {

        this.activatedRoute.parent.params.subscribe((params: Params) => {
            this.params = params;
        });
        this.createConfigForm();
    }

    ngOnInit() {
        this.config = this.hs.getConfigs();
        if (this.config) {
            this.reInitButton();
            this.restoreConfig();
            this.hs.setConfigs(this.config);
        } else {
            this.getConfiguration();
        }
    }

    reInitButton() {
        this.btn = {
            netQueue: {
                count: false,
                rss: false,
            },
            restore: false,
            configDefault: false
        }
    }

    getConfiguration() {
        this.err.getConfiguration = false;
        this.reInitButton();
        this.config = {};
        this.refreshButtonDisabled = true
        this.hs.getConfiguration(this.params['id'])
            .subscribe(
                data => {
                    this.refreshButtonDisabled = false
                    this.config = data;
                    this.restoreConfig();
                    this.hs.setConfigs(data);
                },
                err => {
                    console.error(err);
                    this.refreshButtonDisabled = false
                    this.err.getConfiguration = true;
                }
            );
    }

    createConfigForm() {
        this.hostConfig = new FormGroup({
            netQueue: new FormGroup({
                netQueueCount: new FormControl('', [Validators.required, Validators.min(1)]),
                rss: new FormControl('', [Validators.required, Validators.min(1)]),
                maxNumpCPU: new FormControl('', Validators.required),
            }),
            debuggingMask: new FormGroup({
                utils: new FormControl('', Validators.required),
                mgmt: new FormControl('', Validators.required),
                uplink: new FormControl('', Validators.required),
                transmit: new FormControl('', Validators.required),
                receive: new FormControl('', Validators.required),
                hardware: new FormControl('', Validators.required),
                eventQueue: new FormControl('', Validators.required),
                rss: new FormControl('', Validators.required),
                port: new FormControl('', Validators.required),
                interrupt: new FormControl('', Validators.required),
                commonCode: new FormControl('', Validators.required),
                driver: new FormControl('', Validators.required),
                filter: new FormControl('', Validators.required),
                mcdi: new FormControl('', Validators.required),
                debugMask: new FormControl('')
            }),
            overlay: new FormGroup({
                vxlanOffloadEnable: new FormControl({ value: true, disabled: false }, Validators.required),
                geneveOffloadEnable: new FormControl({ value: true, disabled: true }, Validators.required)
            }),
            restart: new FormControl('')
        })
    }

    restoreConfig() {
        this.hostConfig.reset();
        this.hostConfig.setValue(this.config);
    }

    defaultConfig() {
        this.hostConfig.setValue(this.configDefault);
        this.hostConfig.markAsDirty();
    }

    onSubmit(form) {
        this.submitted = false;
        this.hs.putConfiguration(this.params['id'], form)
            .subscribe(
                data => {
                    this.getConfiguration();
                },
                err => {
                    console.error(err);
                    this.getConfiguration();
                }
            );
    }
    formNotChanged() {
        // {
        //     "netQueue": { "netQueueCount": 8, "rss": 4, "maxNumpCPU": false },
        //     "debuggingMask": {
        //         "utils": true,
        //         "mgmt": false,
        //         "uplink": false,
        //         "transmit": false,
        //         "receive": true,
        //         "hardware": false,
        //         "eventQueue": false,
        //         "rss": false,
        //         "port": false,
        //         "interrupt": false,
        //         "commonCode": false,
        //         "driver": false,
        //         "filter": false,
        //         "mcdi": false
        //     },
        //     "overlay": { "vxlanOffloadEnable": true, "geneveOffloadEnable": true },
        //     "restart": false
        // };
        if(this.config.netQueue.netQueueCount !== this.hostConfig.get('netQueue').get('netQueueCount').value){
            return false;
        }
        if(this.config.netQueue.rss !== this.hostConfig.get('netQueue').get('rss').value){
            return false;
        }
        if(this.config.debuggingMask.utils !== this.hostConfig.get('debuggingMask').get('utils').value){
            return false;
        }
        if(this.config.debuggingMask.mgmt !== this.hostConfig.get('debuggingMask').get('mgmt').value){
            return false;
        }
        if(this.config.debuggingMask.uplink !== this.hostConfig.get('debuggingMask').get('uplink').value){
            return false;
        }
        if(this.config.debuggingMask.transmit !== this.hostConfig.get('debuggingMask').get('transmit').value){
            return false;
        }
        if(this.config.debuggingMask.receive !== this.hostConfig.get('debuggingMask').get('receive').value){
            return false;
        }
        if(this.config.debuggingMask.hardware !== this.hostConfig.get('debuggingMask').get('hardware').value){
            return false;
        }
        if(this.config.debuggingMask.eventQueue !== this.hostConfig.get('debuggingMask').get('eventQueue').value){
            return false;
        }
        if(this.config.debuggingMask.rss !== this.hostConfig.get('debuggingMask').get('rss').value){
            return false;
        }
        if(this.config.debuggingMask.port !== this.hostConfig.get('debuggingMask').get('port').value){
            return false;
        }
        if(this.config.debuggingMask.interrupt !== this.hostConfig.get('debuggingMask').get('interrupt').value){
            return false;
        }
        if(this.config.debuggingMask.commonCode !== this.hostConfig.get('debuggingMask').get('commonCode').value){
            return false;
        }
        if(this.config.debuggingMask.driver !== this.hostConfig.get('debuggingMask').get('driver').value){
            return false;
        }
        if(this.config.debuggingMask.filter !== this.hostConfig.get('debuggingMask').get('filter').value){
            return false;
        }
        if(this.config.debuggingMask.mcdi !== this.hostConfig.get('debuggingMask').get('mcdi').value){
            return false;
        }
        if(this.config.overlay.vxlanOffloadEnable !== this.hostConfig.get('overlay').get('vxlanOffloadEnable').value){
            return false;
        }
        if(this.config.overlay.geneveOffloadEnable !== this.hostConfig.get('overlay').get('geneveOffloadEnable').value){
            return false;
        }
        return true;
    }
}
