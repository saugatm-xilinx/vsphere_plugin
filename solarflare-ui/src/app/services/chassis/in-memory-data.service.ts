import { InMemoryDbService } from "angular-in-memory-web-api";
import { fakeChassisList }       from "../testing/fake-chassis";

export class InMemoryDataService implements InMemoryDbService {
   createDb() {
      return { fakeChassisList };
   }
}
