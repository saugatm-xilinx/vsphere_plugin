// External imports
import { async, ComponentFixture, fakeAsync, TestBed, tick } from "@angular/core/testing";
import { Component, DebugElement }    from "@angular/core";
import { By }           from "@angular/platform-browser";
import { ClarityModule } from "clarity-angular";

// Internal imports
import { EchoService, NavService }  from "../../services/index";
import { GlobalsService, RefreshService,
         AppAlertService, I18nService } from "../../shared/index";
import { MainComponent }     from "./index";
import { globalsServiceStub, appAlertServiceStub,
         echoServiceStub, i18nServiceStub, navServiceStub,
         userSettingServiceStub } from "../../testing/service-stubs";
import { UserSettingService } from "../../shared/user-settings.service";
// [removable-chassis-code]
import { chassisServiceStub } from "../../testing/service-stubs";
import { ChassisService } from "../../services/chassis/chassis.service";
// [end-chassis-code]

// ---------- Testing stubs ------------

@Component({selector: "app-alert", template: ""})
class AppAlertStubComponent {}

@Component({selector: "app-header", template: ""})
class AppHeaderStubComponent {}

@Component({selector: "dialog-box", template: ""})
class DialogBoxStubComponent {}

@Component({selector: "sidenav", template: ""})
class SidenavStubComponent {}


// ----------- Testing vars ------------

let fixture: ComponentFixture<MainComponent>;
let comp: MainComponent;
let compElement;
let page: Page;

// -------------- Tests ----------------

describe("MainComponent", () => {

   beforeEach(async(() => {
      TestBed.configureTestingModule({
         imports: [
            ClarityModule.forRoot()
         ],
         declarations: [
            MainComponent, AppAlertStubComponent,
            AppHeaderStubComponent, DialogBoxStubComponent,
            SidenavStubComponent
         ],
         providers: [ RefreshService,
            { provide: AppAlertService,   useValue: appAlertServiceStub },
            { provide: GlobalsService,    useValue: globalsServiceStub },
            { provide: ChassisService, useValue: chassisServiceStub }, // [removable-chassis-line]
            { provide: EchoService, useValue: echoServiceStub },
            { provide: I18nService, useValue: i18nServiceStub },
            { provide: NavService, useValue: navServiceStub },
            { provide: UserSettingService, useValue: userSettingServiceStub }
         ]
      })
      .compileComponents()
      .then(createComponent);
   }));

   it("should instantiate MainComponent and display the Home tab", () => {
      expect(fixture.componentInstance instanceof MainComponent).toBe(true,
            "should create MainComponent");
      const homeTextKey = compElement.querySelector(".content-area p").innerText;
      expect(homeTextKey).toBe("mainView.content");
   });

   it("should display 2 buttons in Echo Service tab", () => {
      // fixture.detectChanges();
      const tabLinks =  compElement.querySelectorAll(".nav .nav-link");
      tabLinks[1].click();
      const helloButtons = compElement.querySelectorAll(".card-block .btn");
      expect(helloButtons.length).toEqual(2);
      expect(helloButtons[0].id).toBe("helloBtn1");
      expect(helloButtons[1].id).toBe("helloBtn2");
   });

   it("should call echoService.sendEcho for hello button 1", fakeAsync(() => {
      const helloButtons = compElement.querySelectorAll(".card-block .btn");
      helloButtons[0].click();
      expect(<any>page.echoServiceSpy.calls.any()).toBe(true, "sendEcho called");
   }));

   it("should call navService.showSettingsView for settingsButton", fakeAsync(() => {
      const settingsButton = compElement.querySelector(".content-area > .btn-link");
      settingsButton.click();
      expect(<any>page.navServiceSpy.calls.any()).toBe(true, "showSettingsView called");
   }));

});


// -------------- Helpers ----------------

function createComponent() {
   fixture = TestBed.createComponent(MainComponent);
   comp = fixture.componentInstance;
   compElement = fixture.debugElement.nativeElement;

   // Change detection triggers ngOnInit which loads page data
   fixture.detectChanges();

   // Build the page once data is loaded
   return fixture.whenStable().then(() => {
      fixture.detectChanges();
      page = new Page();
   });
}

class Page {
   echoServiceSpy: jasmine.Spy;
   navServiceSpy: jasmine.Spy;

   constructor() {
      // Get the component's injected service and spy on it
      const echoService = fixture.debugElement.injector.get(EchoService);
      this.echoServiceSpy = spyOn(echoService, "sendEcho").and.callThrough();
      const navService = fixture.debugElement.injector.get(NavService);
      this.navServiceSpy = spyOn(navService, "showSettingsView");
   };
}
