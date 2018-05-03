import { Chassis } from "../chassis/chassis.model";
/**
 * Fake chassis data used for standalone mode and for unit testing
 *
 * chassisList is an array of <chassisCount> objects like:
 * {
 *    id: "urn:cr:samples:Chassis:server1%252Fchassis-1",
 *    name: "Chassis 1",
 *    dimensions: "20in x 30in x 17in",
 *    serverType: "Server_Type 0"
 * }
 */

export const initialChassisCount = 30;

export const chassisIdConstant = "urn:cr:samples:Chassis:server1%252Fchassis-";

export const fakeChassisList: Array<Chassis> = new Array(initialChassisCount)
      .fill(undefined).map((val, index) => {
   const id = chassisIdConstant + index;
   const name = "mock-Chassis " + (index + 1);
   const dimensions =  "20in x 30in x 17in";
   const serverType = "Server Type " + (index % 3);
   return new Chassis(id, name, dimensions, serverType);
});



