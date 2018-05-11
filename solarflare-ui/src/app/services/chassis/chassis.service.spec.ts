// External imports
import { async, inject, TestBed } from "@angular/core/testing";
import { HttpModule, Http, Response, ResponseOptions, XHRBackend,
         RequestMethod } from "@angular/http";
import { Router }      from "@angular/router";
import { Observable } from "rxjs/Observable";
import { MockBackend, MockConnection } from "@angular/http/testing";


// Internal imports
import { Chassis, ChassisService, chassisType }     from "./index";
import { fakeChassisList }   from "../testing/fake-chassis";
import { Globals, GlobalsService, APP_CONFIG, AppAlertService } from "../../shared/index";
import { AppErrorHandler } from "../../shared/appErrorHandler";
import { globalStub, appErrorHandlerStub } from "../../testing/index";
import { userSettingServiceStub } from "../../testing/service-stubs";
import { UserSettingService } from "../../shared/user-settings.service";


// ---------- Testing stubs ------------

const routerStub = {};

// ----------- Testing vars ------------

let chassisService: ChassisService;
let globalsService: GlobalsService;
let http: Http;
let httpSpy: any;

let backend: MockBackend;
let obs: Observable<Response>;
let resp: Response;


// -------------- Tests ----------------

describe("ChassisService tests", () => {
   const properties = APP_CONFIG.chassisProperties.toString();

   beforeEach(() => {
      TestBed.configureTestingModule({
         imports: [ HttpModule ],
         providers: [ ChassisService, GlobalsService, AppAlertService,
            { provide: AppErrorHandler, useValue: appErrorHandlerStub },
            { provide: Globals, useValue: globalStub },
            { provide: Router, useValue: routerStub },
            { provide: UserSettingService, useValue: userSettingServiceStub },
            { provide: XHRBackend, useClass: MockBackend }
         ]
      });
      http = TestBed.get(Http);
      chassisService = TestBed.get(ChassisService);
      globalsService = TestBed.get(GlobalsService);
   });

   describe("when useLiveData is true", () => {
      let webContextPath: string;
      beforeEach(() => {
         spyOn(globalsService, "useLiveData").and.returnValue(true);
         webContextPath = globalsService.getWebContextPath();

         // Create an Observable returning a dummy response so that we can spy on http.get
         resp = new Response(new ResponseOptions({status: 200, body: {data: []}}));
         obs =  Observable.create(function(observer) {
            observer.onNext(resp);
            observer.onCompleted();
         });
         httpSpy = spyOn(http, "get").and.returnValue(obs);
      });

      it ("makes the right http call for getChassisList", ()  => {
         chassisService.getChassisList();

         const httpArgs = httpSpy.calls.first().args;
         expect(httpArgs[0]).toBe(webContextPath +
            "/rest/data/list/?targetType=" + chassisType + "&properties=" + properties);
      });

      it ("makes the right http call for getChassis", ()  => {
         const chassis: Chassis = fakeChassisList[0];

         chassisService.getChassis(chassis.id);

         const httpArgs = httpSpy.calls.first().args;
         expect(httpArgs[0]).toBe(webContextPath +
            "/rest/data/properties/" + chassis.id + "?properties=" + properties);
      });

      it ("makes the right http call to update a chassis", ()  => {
         const spy = spyOn(globalsService.getWebPlatform(), "callActionsController");
         const chassis: Chassis = fakeChassisList[0];
         const jsonStr = JSON.stringify(chassis);

         chassisService.save(chassis);

         const args = spy.calls.first().args;
         expect(args[0]).toBe(webContextPath +
            "/rest/actions.html?actionUid=com.solarflare.vcp.editChassis");
         expect(args[1]).toBe(jsonStr);
      });

      it ("makes the right http call to create a chassis", ()  => {
         const spy = spyOn(globalsService.getWebPlatform(), "callActionsController");
         const newChassis: Chassis = Chassis.create();

         const newChassisStr: string = JSON.stringify(newChassis);

         chassisService.save(newChassis);

         const args = spy.calls.first().args;
         expect(args[0]).toBe(webContextPath +
            "/rest/actions.html?actionUid=com.solarflare.vcp.createChassis");
         expect(args[1]).toBe(newChassisStr);
      });
   });

   describe("when useLiveData is false", () => {
      beforeEach(inject([XHRBackend], (mockBackend: MockBackend) => {
         backend = mockBackend;
         spyOn(globalsService, "useLiveData").and.returnValue(false);

         // Create an Observable returning the fakeChassisData
         resp = new Response(new ResponseOptions({status: 200, body: {data: fakeChassisList }}));
         obs =  Observable.create(function(observer) {
            observer.onNext(resp);
            observer.onCompleted();
         });
      }));

      it ("makes the right http call for getChassisList", ()  => {
         const spy = spyOn(http, "get").and.returnValue(obs);

         chassisService.getChassisList();

         const httpArgs = spy.calls.first().args;
         expect(httpArgs[0]).toBe("app/fakeChassisList");
      });

      // Asynchronous tests => use async

      it ("gets the correct chassis mock data", async(()  => {
         backend.connections.subscribe((c: MockConnection) => c.mockRespond(resp));

         chassisService.getChassisList()
            .subscribe(chassisList => {
               expect(<any>chassisList.allChassis.length).toEqual(fakeChassisList.length);
               expect(<any>chassisList.allChassis[0]).toEqual(fakeChassisList[0]);
            });
      }));

      it ("finds the right chassis with getChassis",  async(()  => {
         backend.connections.subscribe((c: MockConnection) => c.mockRespond(resp));
         const chassis1: Chassis = fakeChassisList[1];

         chassisService.getChassis(chassis1.id)
            .map(chassis => {
               expect(chassis).toEqual(fakeChassisList[1]);
            });
      }));

      it ("updates an existing chassis with save" ,  async(()  => {
         // Modify a chassis and check that http.put is called with correct data
         const chassis0: Chassis = fakeChassisList[0];
         const oldChassis0: Chassis = chassis0.clone();
         chassis0.name = "new name";
         chassis0.dimensions = "new dimensions";

         const chassis0Str: string = JSON.stringify(chassis0);
         const saveResponse = new Response(new ResponseOptions({status: 200, body: {data: chassis0Str }}));

         backend.connections.subscribe((c: MockConnection) => {
            if (c.request.method === RequestMethod.Get) {
               c.mockRespond(resp);
            } else if (c.request.method === RequestMethod.Put) {
               expect(c.request.getBody()).toEqual(chassis0Str);
               c.mockRespond(saveResponse);
            } else {
               fail("wrong request");
            }
         });

         chassisService.save(chassis0)
            .then((resp0: any) => {
               expect(resp0.status).toEqual(200);
               // reset chassis0 original values for other tests
               fakeChassisList[0] = oldChassis0;
            });
      }));

      it ("returns an error when chassis name already exists" ,  async(()  => {
         backend.connections.subscribe((c: MockConnection) => c.mockRespond(resp));
         const chassis0: Chassis = fakeChassisList[0];
         const existingName = fakeChassisList[1].name;
         chassis0.name = existingName;

         chassisService.save(chassis0)
            .then(chassis => {
               fail("should not respond here");
            })
            .catch(err => {
               expect(err).toMatch(existingName + " already exists!");
            });
      }));

      it ("creates a new chassis with save" ,  async(()  => {
         const newChassis: Chassis = Chassis.create();

         const newChassisStr: string = JSON.stringify(newChassis);
         const saveResponse = new Response(new ResponseOptions({status: 200, body: {data: newChassisStr }}));

         backend.connections.subscribe((c: MockConnection) => {
            if (c.request.method === RequestMethod.Get) {
               c.mockRespond(resp);
            } else if (c.request.method === RequestMethod.Post) {
               c.mockRespond(saveResponse);
            } else {
               fail("wrong request");
            }
         });

         chassisService.save(newChassis)
            .then(res => {
               expect(<any>resp.status).toEqual(200);
               // Note that we can't check if newChassis was created by
               // InMemoryDataService here, because it is not under test
            });

      }));

      it ("deletes an existing chassis" ,  async(()  => {
         const chassis0: Chassis = fakeChassisList[0];
         const deleteUrl = "app/fakeChassisList/" + `${chassis0.id}`;
         const delResponse = new Response(new ResponseOptions({status: 200 }));

         backend.connections.subscribe((c: MockConnection) => {
            if (c.request.method === RequestMethod.Delete) {
               // http.delete should be called with the correct url
               expect(c.request.url).toEqual(deleteUrl);
               c.mockRespond(delResponse);
            } else {
               fail("wrong request");
            }
         });

         chassisService.delete(chassis0)
            .then(response => {
               expect(<any>response.status).toEqual(200);
            });
      }));
   });
});
