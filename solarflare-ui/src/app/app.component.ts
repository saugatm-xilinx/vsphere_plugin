import {Component, Injector, ChangeDetectorRef, OnInit} from "@angular/core";
import {GlobalsService, RefreshService, I18nService} from "./shared/index";
import {ActionDevService} from "./services/testing/action-dev.service";
import {AppMainService} from "./services/app-main.service";
// TODO: review comment:- Many linting issues in entire project.

@Component({
    selector: "my-app",
    styleUrls: ["./app.component.scss"],
    templateUrl: "./app.component.html",
    providers: []
})

export class AppComponent implements OnInit {
    public hosts = [];
    public Collapsible = true;
    public getHostsErr = false;

    constructor(public  gs: GlobalsService,
                private injector: Injector,
                private refreshService: RefreshService,
                private i18nService: I18nService,
                private changeDetector: ChangeDetectorRef,
                private as: AppMainService) {

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
// TODO: reivew comment - implement onInit in the class.
    ngOnInit(): void {
        this.getHosts();
    }

    getHosts() {
        this.getHostsErr = false;
        this.as.getHosts()
            .subscribe(
                data => {
                    this.hosts = data
                },
                err => {
                    console.error(err);
                    this.getHostsErr = true;
                }
            );
    }
// TODO: review comments- Create a localDev file/class and put all local development code there. AppComponent
// is having devMode function. This gets added in bundle files during
// build process. Better to keep mock data in a file and add/remove its dependency as per environment mode - prod or dev.

    refresh(): void {
        // This propagates the refresh event to views that have subscribed to the RefreshService
        this.refreshService.refreshView();

        if (this.gs.isPluginMode()) {
            // This helps refresh the app's children components in Plugin mode after refreshView
            this.changeDetector.detectChanges();
        }
    }
}





