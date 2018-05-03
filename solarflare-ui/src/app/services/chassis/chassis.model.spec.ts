import { Chassis } from "./chassis.model";
import { fakeChassisList } from "../testing/fake-chassis";


// ----------- Testing vars ------------

const chassis0: Chassis = fakeChassisList[0];


// ----------- Tests ------------

describe("Chassis", () => {
   it("has id, name, dimensions, serverType", () => {
      const chassis = new Chassis(chassis0.id, chassis0.name, chassis0.dimensions, chassis0.serverType);
      expect(chassis.id).toBe(chassis0.id);
      expect(chassis.name).toBe(chassis0.name);
      expect(chassis.dimensions).toBe(chassis0.dimensions);
      expect(chassis.serverType).toBe(chassis0.serverType);
   });

   it("can clone itself", () => {
      const chassis = chassis0.clone();
      expect(<any>chassis0).toEqual(chassis);
   });

   it("equals another chassis with same properties", () => {
      const chassis1 = chassis0.clone();
      chassis1.id = "id1";
      const chassis2 = chassis0.clone();
      chassis2.id = "id2";
      expect(chassis1.equals(chassis2)).toBeTruthy();
   });

});
