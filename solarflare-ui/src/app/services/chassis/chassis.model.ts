/**
 * A simple Chassis model
 */
export class Chassis {

   constructor(public id: string, public name: string,
               public dimensions: string, public serverType: string) {
   }

   // Warning: methods defined below can only be used on chassis objects created explicitly.
   // i.e. they won't exist on chassis objects converted from Json data

   clone(): Chassis {
      return new Chassis(this.id, this.name, this.dimensions, this.serverType);
   }

   equals(other: Chassis): boolean {
      return other &&
         (this.name === other.name && this.dimensions === other.dimensions && this.serverType === other.serverType);
   }

   static create() {
      return new Chassis(null, "", "", "");
   }
}

/**
 * An object for reporting errors while getting chassis data
 */
export class ChassisError {
   constructor(public id: string, public error: string) {
   }
}