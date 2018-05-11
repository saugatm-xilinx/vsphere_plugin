/**
 * A simple Host object model
 */
export class Host {
   id: string;
   /** Host name */
   name: string;
   /** Host overallStatus: green, yellow, red */
   status: string;
   /** Type of host, from property "hardware.systemInfo.model" */
   model: string;
   /** List of vm ids */
   vms: Array<string> = [];

   constructor(id: string, name: string) {
      this.id = id;
      this.name = name;
   }

   /**
    * Conversion between the vSphere property names and this model
    * @param hostData
    * @returns {Host}
    */
   static convertProperties(hostData: any, useLiveData: boolean): Host {
      const host = new Host(hostData.id, hostData.name);

      if (useLiveData) {
         host.status = hostData["overallStatus"];
         host.model = hostData["hardware.systemInfo.model"];

         // The hostData.vm property returned by the backend is an array of VM objects like
         // {value: "vm-158", type: "VirtualMachine", serverGuid: "8d88c879-6e53-4d70-a00b-9a725d121077"}
         // We only keep the values for now
         host.vms = hostData.vm ? hostData.vm.map(val => val.value) : [] ;

      } else {
         // Case of mock data, no conversion necessary.
         host.status = hostData["status"];
         host.model = hostData["model"];
         host.vms = hostData.vms;
      }
      return host
   }
}

/**
 * An object for reporting errors while getting host data
 */
export class HostError {
   constructor(public id: string, public error: string) {
   }
}
