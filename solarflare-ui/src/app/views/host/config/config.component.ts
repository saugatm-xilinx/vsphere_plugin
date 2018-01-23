import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Params} from '@angular/router';
import {GlobalsService} from "../../../shared/globals.service";
import {HostsService} from "../../../services/hosts.service";
import {FormBuilder, FormGroup, FormControl, Validators} from "@angular/forms";
//TODO :- reveiw comments - It would be best if import is done via a absolute path.
// Define all modules paths in tsconfig.json file. This will help in managing file import.
    
@Component({
    selector: 'app-config',
    templateUrl: './config.component.html',
    styleUrls: ['./config.component.scss']
})
export class ConfigComponent implements OnInit {
    public params = {};
    hostConfig: FormGroup;
    public configDefault = {
        "netQueue": {"netQueueCount": 8, "rss": 4, "maxNumpCPU": false},
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
            "filter": false
        },
        "overlay": {"vxlanOffloadEnable": true, "geneveOffloadEnable": true},
        "restart": false
    };
    public config = {};
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

    loadConfig(){
        this.err.getConfiguration = false;
        this.config = {
            "netQueue": {"netQueueCount": 4, "rss": 1, "maxNumpCPU": true},
            "debuggingMask": {
                "utils": true,
                "mgmt": true,
                "uplink": false,
                "transmit": false,
                "receive": false,
                "hardware": false,
                "eventQueue": false,
                "rss": false,
                "port": false,
                "interrupt": false,
                "commonCode": false,
                "driver": true,
                "filter": false
            },
            "overlay": {"vxlanOffloadEnable": true, "geneveOffloadEnable": true},
            "restart": false
        };
    }

    ngOnInit() {
        this.getConfiguration();
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

        this.hs.getConfiguration(this.params['id'])
            .subscribe(
                data => {
                    this.config = data;
                    this.restoreConfig();
                },
                err => {
                    console.error(err);
                    this.err.getConfiguration = true;
                    // COMMENT THIS SECTION IN PROD - START
                    //this.loadConfig();
                    //this.restoreConfig();
                    // COMMENT THIS SECTION IN PROD - END
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
                filter: new FormControl('', Validators.required)
            }),
            overlay: new FormGroup({
                vxlanOffloadEnable: new FormControl('', Validators.required),
                geneveOffloadEnable: new FormControl('', Validators.required)
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

        //console.log(form);

        this.hs.putConfiguration(this.params['id'], form)
            .subscribe(
                data => {
                    //console.log(data);
                    this.getConfiguration();
                },
                err => {
                    console.error(err);
                    this.getConfiguration();
                }
            );
    }

}
